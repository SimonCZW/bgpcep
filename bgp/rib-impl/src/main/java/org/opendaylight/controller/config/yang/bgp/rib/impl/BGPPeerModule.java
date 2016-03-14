/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: bgp-rib-impl  yang module local name: bgp-peer
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Sat Jan 25 11:00:14 CET 2014
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.bgp.rib.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.protocol.bgp.openconfig.spi.BGPConfigModuleTracker;
import org.opendaylight.protocol.bgp.openconfig.spi.BGPOpenConfigProvider;
import org.opendaylight.protocol.bgp.openconfig.spi.BGPOpenconfigMapper;
import org.opendaylight.protocol.bgp.openconfig.spi.InstanceConfigurationIdentifier;
import org.opendaylight.protocol.bgp.openconfig.spi.pojo.BGPPeerInstanceConfiguration;
import org.opendaylight.protocol.bgp.parser.BgpTableTypeImpl;
import org.opendaylight.protocol.bgp.parser.spi.MultiprotocolCapabilitiesUtil;
import org.opendaylight.protocol.bgp.rib.impl.BGPPeer;
import org.opendaylight.protocol.bgp.rib.impl.StrictBGPPeerRegistry;
import org.opendaylight.protocol.bgp.rib.impl.spi.BGPPeerRegistry;
import org.opendaylight.protocol.bgp.rib.impl.spi.BGPSessionPreferences;
import org.opendaylight.protocol.bgp.rib.impl.spi.RIB;
import org.opendaylight.protocol.util.Ipv6Util;
import org.opendaylight.tcpmd5.api.KeyMapping;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.BgpParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.BgpParametersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.bgp.parameters.OptionalCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.bgp.parameters.OptionalCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.bgp.parameters.optional.capabilities.CParametersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.bgp.parameters.optional.capabilities.c.parameters.As4BytesCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.BgpTableType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.CParameters1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.CParameters1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.mp.capabilities.AddPathCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.mp.capabilities.GracefulRestartCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.mp.capabilities.MultiprotocolCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.mp.capabilities.add.path.capability.AddressFamilies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.PeerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.tcpmd5.cfg.rev140427.Rfc2385Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class BGPPeerModule extends org.opendaylight.controller.config.yang.bgp.rib.impl.AbstractBGPPeerModule {
    private static final Logger LOG = LoggerFactory.getLogger(BGPPeerModule.class);

    public BGPPeerModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
        final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public BGPPeerModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
        final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, final BGPPeerModule oldModule,
        final java.lang.AutoCloseable oldInstance) {

        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    protected void customValidation() {
        final IpAddress host = getHost();
        JmxAttributeValidationException.checkNotNull(host, "value is not set.", hostJmxAttribute);
        JmxAttributeValidationException.checkCondition(host.getIpv4Address() != null || host.getIpv6Address() != null,
            "Unexpected host", hostJmxAttribute);

        JmxAttributeValidationException.checkNotNull(getPort(), "value is not set.", portJmxAttribute);

        if (getOptionaPassword(getPassword()).isPresent()) {
            /*
             *  This is a nasty hack, but we don't have another clean solution. We cannot allow
             *  password being set if the injected dispatcher does not have the optional
             *  md5-server-channel-factory set.
             *
             *  FIXME: this is a use case for Module interfaces, e.g. RibImplModule
             *         should something like isMd5ServerSupported()
             */

            final RIBImplModuleMXBean ribProxy = this.dependencyResolver.newMXBeanProxy(getRib(), RIBImplModuleMXBean.class);
            final BGPDispatcherImplModuleMXBean bgpDispatcherProxy = this.dependencyResolver.newMXBeanProxy(
                ribProxy.getBgpDispatcher(), BGPDispatcherImplModuleMXBean.class);
            final boolean isMd5Supported = bgpDispatcherProxy.getMd5ChannelFactory() != null;

            JmxAttributeValidationException.checkCondition(isMd5Supported,
                "Underlying dispatcher does not support MD5 clients", passwordJmxAttribute);

        }

        if (getPeerRole() != null) {
            final boolean isNotPeerRoleInternal= getPeerRole() != PeerRole.Internal;
            JmxAttributeValidationException.checkCondition(isNotPeerRoleInternal,
                "Internal Peer Role is reserved for Application Peer use.", peerRoleJmxAttribute);
        }
    }

    private InetSocketAddress createAddress() {
        final IpAddress ip = getHost();
        Preconditions.checkArgument(ip.getIpv4Address() != null || ip.getIpv6Address() != null, "Failed to handle host %s", ip);
        if (ip.getIpv4Address() != null) {
            return new InetSocketAddress(InetAddresses.forString(ip.getIpv4Address().getValue()), getPort().getValue());
        }
        return new InetSocketAddress(InetAddresses.forString(ip.getIpv6Address().getValue()), getPort().getValue());
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final RIB r = getRibDependency();

        final List<BgpParameters> tlvs = getTlvs(r);
        final AsNumber remoteAs = getAsOrDefault(r);
        final BGPSessionPreferences prefs = new BGPSessionPreferences(r.getLocalAs(), getHoldtimer(), r.getBgpIdentifier(), remoteAs, tlvs);
        final BGPPeer bgpClientPeer;
        final IpAddress host = getNormalizedHost();
        if (getPeerRole() != null) {
            bgpClientPeer = new BGPPeer(peerName(host), r, getPeerRole());
        } else {
            bgpClientPeer = new BGPPeer(peerName(host), r, PeerRole.Ibgp);
        }

        bgpClientPeer.registerRootRuntimeBean(getRootRuntimeBeanRegistratorWrapper());

        getPeerRegistryBackwards().addPeer(host, bgpClientPeer, prefs);

        final BGPPeerModuleTracker moduleTracker = new BGPPeerModuleTracker(r.getOpenConfigProvider());
        moduleTracker.onInstanceCreate();

        final CloseableNoEx peerCloseable = new CloseableNoEx() {
            @Override
            public void close() {
                bgpClientPeer.close();
                getPeerRegistryBackwards().removePeer(host);
                moduleTracker.onInstanceClose();
            }
        };

        // Initiate connection
        if(getInitiateConnection()) {
            final Future<Void> cf = initiateConnection(createAddress(), getOptionaPassword(getPassword()), getPeerRegistryBackwards());
            return new CloseableNoEx() {
                @Override
                public void close() {
                    cf.cancel(true);
                    peerCloseable.close();
                }
            };
        } else {
            return peerCloseable;
        }
    }

    private interface CloseableNoEx extends AutoCloseable {
        @Override
        void close();
    }

    private AsNumber getAsOrDefault(final RIB r) {
        // Remote AS number defaults to our local AS
        final AsNumber remoteAs;
        if (getRemoteAs() != null) {
            remoteAs = new AsNumber(getRemoteAs());
        } else {
            remoteAs = r.getLocalAs();
        }
        return remoteAs;
    }

    private List<BgpParameters> getTlvs(final RIB r) {
        final List<BgpParameters> tlvs = new ArrayList<>();
        final List<OptionalCapabilities> caps = new ArrayList<>();
        caps.add(new OptionalCapabilitiesBuilder().setCParameters(new CParametersBuilder().setAs4BytesCapability(
            new As4BytesCapabilityBuilder().setAsNumber(r.getLocalAs()).build()).build()).build());
        caps.add(new OptionalCapabilitiesBuilder().setCParameters(new CParametersBuilder().addAugmentation(CParameters1.class,
            new CParameters1Builder().setGracefulRestartCapability(new GracefulRestartCapabilityBuilder().build()).build()).build()).build());

        if (getRouteRefresh()) {
            caps.add(new OptionalCapabilitiesBuilder().setCParameters(MultiprotocolCapabilitiesUtil.RR_CAPABILITY).build());
        }

        if (!getAddPathDependency().isEmpty()) {
            final List<AddressFamilies> addPathFamilies = filterAddPathDependency(getAddPathDependency());
            caps.add(new OptionalCapabilitiesBuilder().setCParameters(new CParametersBuilder().addAugmentation(CParameters1.class,
                new CParameters1Builder().setAddPathCapability(new AddPathCapabilityBuilder().setAddressFamilies(addPathFamilies).build()).build()).build()).build());
        }

        for (final BgpTableType t : getAdvertizedTableDependency()) {
            if (!r.getLocalTables().contains(t)) {
                LOG.info("RIB instance does not list {} in its local tables. Incoming data will be dropped.", t);
            }

            caps.add(new OptionalCapabilitiesBuilder().setCParameters(new CParametersBuilder().addAugmentation(CParameters1.class,
                new CParameters1Builder().setMultiprotocolCapability(new MultiprotocolCapabilityBuilder(t).build()).build()).build()).build());
        }
        tlvs.add(new BgpParametersBuilder().setOptionalCapabilities(caps).build());
        return tlvs;
    }

    private List<AddressFamilies> filterAddPathDependency(final List<AddressFamilies> addPathDependency) {
        final Map<BgpTableType, AddressFamilies> filteredFamilies = new HashMap<BgpTableType, AddressFamilies>();
        for (final AddressFamilies family : addPathDependency) {
            final BgpTableType key = new BgpTableTypeImpl(family.getAfi(), family.getSafi());
            if (!filteredFamilies.containsKey(key)) {
                filteredFamilies.put(key, family);
            } else {
                LOG.info("Ignoring Add-path dependency {}", family);
            }
        }
        return new ArrayList<AddressFamilies>(filteredFamilies.values());
    }

    public IpAddress getNormalizedHost() {
        final IpAddress host = getHost();
        if(host.getIpv6Address() != null){
            return new IpAddress(Ipv6Util.getFullForm(host.getIpv6Address()));
        }
        return host;
    }

    private io.netty.util.concurrent.Future<Void> initiateConnection(final InetSocketAddress address, final Optional<Rfc2385Key> password, final BGPPeerRegistry registry) {
        KeyMapping keys = null;
        if (password.isPresent()) {
            keys = new KeyMapping();
            keys.put(address.getAddress(), password.get().getValue().getBytes(Charsets.US_ASCII));
        }

        final RIB rib = getRibDependency();
        final Optional<KeyMapping> optionalKey = Optional.fromNullable(keys);
        return rib.getDispatcher().createReconnectingClient(address, registry, rib.getTcpStrategyFactory(), optionalKey);
    }

    private BGPPeerRegistry getPeerRegistryBackwards() {
        return getPeerRegistry() == null ? StrictBGPPeerRegistry.GLOBAL : getPeerRegistryDependency();
    }

    private static String peerName(final IpAddress host) {
        if (host.getIpv4Address() != null) {
            return host.getIpv4Address().getValue();
        }
        if (host.getIpv6Address() != null) {
            return host.getIpv6Address().getValue();
        }

        return null;
    }

    private final class BGPPeerModuleTracker implements BGPConfigModuleTracker {

        private final BGPOpenconfigMapper<BGPPeerInstanceConfiguration> neighborProvider;
        private final BGPPeerInstanceConfiguration bgpPeerInstanceConfiguration;

        public BGPPeerModuleTracker(final Optional<BGPOpenConfigProvider> openconfigProvider) {
            if (openconfigProvider.isPresent()) {
                this.neighborProvider = openconfigProvider.get().getOpenConfigMapper(BGPPeerInstanceConfiguration.class);
            } else {
                this.neighborProvider = null;
            }
            final InstanceConfigurationIdentifier identifier = new InstanceConfigurationIdentifier(getIdentifier().getInstanceName());
            this.bgpPeerInstanceConfiguration = new BGPPeerInstanceConfiguration(identifier, Rev130715Util.getIpvAddress(getNormalizedHost()),
                    Rev130715Util.getPort(getPort().getValue()), getHoldtimer(), getPeerRole(), getInitiateConnection(),
                        getAdvertizedTableDependency(), Rev130715Util.getASNumber(getAsOrDefault(getRibDependency()).getValue()),
                        getOptionaPassword(getPassword()));
        }

        @Override
        public void onInstanceCreate() {
            if (this.neighborProvider != null) {
                this.neighborProvider.writeConfiguration(this.bgpPeerInstanceConfiguration);
            }
        }

        @Override
        public void onInstanceClose() {
            if (this.neighborProvider != null) {
                this.neighborProvider.removeConfiguration(this.bgpPeerInstanceConfiguration);
            }
        }

    }

    private Optional<Rfc2385Key> getOptionaPassword(final Rfc2385Key password) {
        return password != null && ! password.getValue().isEmpty() ? Optional.of(password) : Optional.<Rfc2385Key>absent();
    }

}
