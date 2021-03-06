/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.evpn.impl.esi.types;

import java.util.List;
import org.opendaylight.protocol.bgp.evpn.spi.pojo.SimpleEsiTypeRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.ArbitraryCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.AsGeneratedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.LacpAutoGeneratedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.LanAutoGeneratedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.MacAutoGeneratedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.RouterIdGeneratedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.arbitrary._case.Arbitrary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.as.generated._case.AsGenerated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.lacp.auto.generated._case.LacpAutoGenerated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.lan.auto.generated._case.LanAutoGenerated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.mac.auto.generated._case.MacAutoGenerated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev160321.esi.esi.router.id.generated._case.RouterIdGenerated;

public final class ESIActivator {
    private ESIActivator() {
        throw new UnsupportedOperationException();
    }

    public static void registerEsiTypeParsers(final List<AutoCloseable> regs) {
        final SimpleEsiTypeRegistry esiRegistry = SimpleEsiTypeRegistry.getInstance();

        final ArbitraryParser t0Parser = new ArbitraryParser();
        regs.add(esiRegistry.registerEsiParser(t0Parser.getType(), t0Parser));
        regs.add(esiRegistry.registerEsiSerializer(ArbitraryCase.class, t0Parser));
        regs.add(esiRegistry.registerEsiModelSerializer(Arbitrary.QNAME, t0Parser));

        final LacpParser t1Parser = new LacpParser();
        regs.add(esiRegistry.registerEsiParser(t1Parser.getType(), t1Parser));
        regs.add(esiRegistry.registerEsiSerializer(LacpAutoGeneratedCase.class, t1Parser));
        regs.add(esiRegistry.registerEsiModelSerializer(LacpAutoGenerated.QNAME, t1Parser));

        final LanParser t2Parser = new LanParser();
        regs.add(esiRegistry.registerEsiParser(t2Parser.getType(), t2Parser));
        regs.add(esiRegistry.registerEsiSerializer(LanAutoGeneratedCase.class, t2Parser));
        regs.add(esiRegistry.registerEsiModelSerializer(LanAutoGenerated.QNAME, t2Parser));

        final MacParser t3Parser = new MacParser();
        regs.add(esiRegistry.registerEsiParser(t3Parser.getType(), t3Parser));
        regs.add(esiRegistry.registerEsiSerializer(MacAutoGeneratedCase.class, t3Parser));
        regs.add(esiRegistry.registerEsiModelSerializer(MacAutoGenerated.QNAME, t3Parser));

        final RouterIdParser t4Parser = new RouterIdParser();
        regs.add(esiRegistry.registerEsiParser(t4Parser.getType(), t4Parser));
        regs.add(esiRegistry.registerEsiSerializer(RouterIdGeneratedCase.class, t4Parser));
        regs.add(esiRegistry.registerEsiModelSerializer(RouterIdGenerated.QNAME, t4Parser));

        final ASGenParser t5Parser = new ASGenParser();
        regs.add(esiRegistry.registerEsiParser(t5Parser.getType(), t5Parser));
        regs.add(esiRegistry.registerEsiSerializer(AsGeneratedCase.class, t5Parser));
        regs.add(esiRegistry.registerEsiModelSerializer(AsGenerated.QNAME, t5Parser));
    }
}
