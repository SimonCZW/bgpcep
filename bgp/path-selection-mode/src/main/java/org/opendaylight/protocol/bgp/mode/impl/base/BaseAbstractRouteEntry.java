/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.mode.impl.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.UnsignedInteger;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.protocol.bgp.mode.api.BestPath;
import org.opendaylight.protocol.bgp.mode.spi.AbstractRouteEntry;
import org.opendaylight.protocol.bgp.rib.spi.ExportPolicyPeerTracker;
import org.opendaylight.protocol.bgp.rib.spi.PeerExportGroup;
import org.opendaylight.protocol.bgp.rib.spi.RIBSupport;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.PeerId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.PeerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.TablesKey;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
abstract class BaseAbstractRouteEntry extends AbstractRouteEntry {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAbstractRouteEntry.class);
    private static final ContainerNode[] EMPTY_ATTRIBUTES = new ContainerNode[0];
    private OffsetMap offsets = OffsetMap.EMPTY;
    private ContainerNode[] values = EMPTY_ATTRIBUTES;
    private BaseBestPath bestPath;
    private BaseBestPath removedBestPath;

    private int addRoute(final UnsignedInteger routerId, final ContainerNode attributes) {
        int offset = this.offsets.offsetOf(routerId);
        if (offset < 0) {
            final OffsetMap newOffsets = this.offsets.with(routerId);
            offset = newOffsets.offsetOf(routerId);

            this.values = newOffsets.expand(this.offsets, this.values, offset);
            this.offsets = newOffsets;
        }

        this.offsets.setValue(this.values, offset, attributes);
        LOG.trace("Added route from {} attributes {}", routerId, attributes);
        return offset;
    }

    /**
     * Remove route
     *
     * @param routerId router ID in unsigned integer
     * @param offset of removed route
     * @return true if its the last route
     */
    protected final boolean removeRoute(final UnsignedInteger routerId, final int offset) {
        this.values = this.offsets.removeValue(this.values, offset);
        this.offsets = this.offsets.without(routerId);
        return this.offsets.isEmpty();
    }

    // select Best
    //  在loc-rib-writer中调用入口
    @Override
    public final boolean selectBest(final long localAs) {
        /*
         * FIXME: optimize flaps by making sure we consider stability of currently-selected route.
         */
        // 实例化base path selector
        final BasePathSelector selector = new BasePathSelector(localAs);

        // Select the best route.
        for (int i = 0; i < this.offsets.size(); ++i) {
            final UnsignedInteger routerId = this.offsets.getRouterKey(i);
            final ContainerNode attributes = this.offsets.getValue(this.values, i);
            LOG.trace("Processing router id {} attributes {}", routerId, attributes);
            // 从entry的offsets取出路由处理path
            selector.processPath(routerId, attributes);
        }

        // Get the newly-selected best path.
        final BaseBestPath newBestPath = selector.result();
        // 判断是否有修改,如果bestPath改变了则存最新最优的，并返回boolean
        final boolean modified = newBestPath == null || !newBestPath.equals(this.bestPath);
        if (modified) {
            if(this.offsets.isEmpty()) {
                // modified为true, 即有更改, 把removedBestPath定义为现在的bestPath（旧的)
                this.removedBestPath = this.bestPath;
            }
            LOG.trace("Previous best {}, current best {}", this.bestPath, newBestPath);
            // 定义新的bestPath
            this.bestPath = newBestPath;
        }
        return modified;
    }

    @Override
    public int addRoute(final UnsignedInteger routerId, final Long remotePathId, final NodeIdentifier attributesIdentifier, final NormalizedNode<?, ?> data) {
        LOG.trace("Find {} in {}", attributesIdentifier, data);
        final ContainerNode advertisedAttrs = (ContainerNode) NormalizedNodes.findNode(data, attributesIdentifier).orNull();
        return addRoute(routerId, advertisedAttrs);
    }

    // 更新路由信息
    @Override
    public void updateRoute(final TablesKey localTK, final ExportPolicyPeerTracker peerPT, final YangInstanceIdentifier locRibTarget, final RIBSupport ribSup,
        final DOMDataWriteTransaction tx, final PathArgument routeIdPA) {
        // 在selectBest方法调用的时候，有更优路由，会把旧的bestPath赋值到removedBestPath
        if (this.removedBestPath != null) {
            removePathFromDataStore(this.removedBestPath, routeIdPA, locRibTarget, peerPT, localTK, ribSup, tx);
            this.removedBestPath = null;
        }
        // selectBest中会赋值
        if (this.bestPath != null) {
            addPathToDataStore(this.bestPath, routeIdPA, locRibTarget, ribSup, peerPT, localTK, tx);
        }
    }

    @Override
    public void writeRoute(final PeerId destPeer, final PathArgument routeId, final YangInstanceIdentifier rootPath, final PeerExportGroup peerGroup,
        final TablesKey localTK, final ExportPolicyPeerTracker peerPT, final RIBSupport ribSupport, final DOMDataWriteTransaction tx) {
        if (this.bestPath != null) {
            final BaseBestPath path = this.bestPath;
            final PeerRole destPeerRole = getRoutePeerIdRole(peerPT, destPeer);
            if (filterRoutes(path.getPeerId(), destPeer, peerPT, localTK, destPeerRole)) {
                final ContainerNode effAttrib = peerGroup.effectiveAttributes(getRoutePeerIdRole(peerPT,path.getPeerId()), path.getAttributes());
                writeRoute(destPeer, getAdjRibOutYII(ribSupport, rootPath, routeId, localTK), effAttrib,
                    createValue(routeId, path), ribSupport, tx);
            }
        }
    }

    private void removePathFromDataStore(final BestPath path, final PathArgument routeIdPA, final YangInstanceIdentifier locRibTarget,
        final ExportPolicyPeerTracker peerPT, final TablesKey localTK, final RIBSupport ribSup, final DOMDataWriteTransaction tx) {
        LOG.trace("Best Path removed {}", path);
        final PathArgument routeIdAddPath = ribSup.getRouteIdAddPath(path.getPathId(), routeIdPA);
        final YangInstanceIdentifier pathTarget = ribSup.routePath(locRibTarget.node(ROUTES_IDENTIFIER), routeIdPA);
        YangInstanceIdentifier pathAddPathTarget = null;
        if (routeIdAddPath != null) {
            pathAddPathTarget = ribSup.routePath(locRibTarget.node(ROUTES_IDENTIFIER), routeIdAddPath);
        }
        fillLocRib(pathAddPathTarget == null ? pathTarget : pathAddPathTarget, null, tx);
        fillAdjRibsOut(null, null, routeIdPA, path.getPeerId(), peerPT, localTK, ribSup, tx);
    }

    private void addPathToDataStore(final BestPath path, final PathArgument routeIdPA, final YangInstanceIdentifier locRibTarget,
        final RIBSupport ribSup, final ExportPolicyPeerTracker peerPT, final TablesKey localTK, final DOMDataWriteTransaction tx) {
        final PathArgument routeIdAddPath = ribSup.getRouteIdAddPath(path.getPathId(), routeIdPA);
        final YangInstanceIdentifier pathTarget = ribSup.routePath(locRibTarget.node(ROUTES_IDENTIFIER), routeIdPA);
        final NormalizedNode<?, ?> value = createValue(routeIdPA, path);
        NormalizedNode<?, ?> addPathValue = null;
        YangInstanceIdentifier pathAddPathTarget = null;
        if (routeIdAddPath == null) {
            LOG.trace("Selected best value {}", value);
        } else {
            pathAddPathTarget = ribSup.routePath(locRibTarget.node(ROUTES_IDENTIFIER), routeIdAddPath);
            addPathValue = createValue(routeIdAddPath, path);
            LOG.trace("Selected best value {}", addPathValue);
        }
        fillLocRib(pathAddPathTarget == null ? pathTarget : pathAddPathTarget, addPathValue == null ? value : addPathValue, tx);
        fillAdjRibsOut(path.getAttributes(), value, routeIdPA, path.getPeerId(), peerPT, localTK, ribSup, tx);
    }

    final OffsetMap getOffsets() {
        return this.offsets;
    }

    // 过 export policy，再写路由到adj-rib-out
    @VisibleForTesting
    private void fillAdjRibsOut(final ContainerNode attributes, final NormalizedNode<?, ?> value,
        final PathArgument routeId, final PeerId routePeerId, final ExportPolicyPeerTracker peerPT,
        final TablesKey localTK, final RIBSupport ribSup, final DOMDataWriteTransaction tx) {
        /*
         * We need to keep track of routers and populate adj-ribs-out, too. If we do not, we need to
         * expose from which client a particular route was learned from in the local RIB, and have
         * the listener perform filtering.
         *
         * We walk the policy set in order to minimize the amount of work we do for multiple peers:
         * if we have two eBGP peers, for example, there is no reason why we should perform the translation
         * multiple times.
         */
        // 轮询所有peerRole, 并获取其对应peerGroup，
        for (final PeerRole role : PeerRole.values()) {
            // exprot policy peer tarcker记录了需要export的peer信息
            // 这里会找到 bgpPeer(如果是applicationPeer发路由)
            final PeerExportGroup peerGroup = peerPT.getPeerGroup(role);
            if (peerGroup != null) {
                // 过滤export policy规则
                final ContainerNode effAttrib = peerGroup.effectiveAttributes(getRoutePeerIdRole(peerPT, routePeerId),
                    attributes);
                peerGroup.forEach((destPeer, rootPath) -> {
                    if (!filterRoutes(routePeerId, destPeer, peerPT, localTK, getRoutePeerIdRole(peerPT, destPeer))) {
                        return;
                    }
                    update(destPeer, getAdjRibOutYII(ribSup, rootPath, routeId, localTK), effAttrib, value, ribSup, tx);
                });
            }
        }
    }
}