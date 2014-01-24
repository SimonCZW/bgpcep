/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.ietf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.protocol.pcep.ietf.initiated00.PCEPLspObjectParser;
import org.opendaylight.protocol.pcep.ietf.initiated00.PCEPSrpObjectParser;
import org.opendaylight.protocol.pcep.ietf.stateful07.PCEPLspaObjectParser;
import org.opendaylight.protocol.pcep.ietf.stateful07.PCEPOpenObjectParser;
import org.opendaylight.protocol.pcep.spi.ObjectHeaderImpl;
import org.opendaylight.protocol.pcep.spi.PCEPDeserializerException;
import org.opendaylight.protocol.pcep.spi.TlvHandlerRegistry;
import org.opendaylight.protocol.pcep.spi.pojo.ServiceLoaderPCEPExtensionProviderContext;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.Lsp1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.Lsp1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.Srp1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.Srp1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.OperationalStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.PlspId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.SrpIdNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.Tlvs2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.Tlvs2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.error.code.tlv.LspErrorCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.error.code.tlv.LspErrorCodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.LspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.srp.object.SrpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.stateful.capability.tlv.Stateful;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.stateful.capability.tlv.StatefulBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.symbolic.path.name.tlv.SymbolicPathName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.symbolic.path.name.tlv.SymbolicPathNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.ProtocolVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.lspa.object.LspaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.object.OpenBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev130820.AttributeFilter;

public class PCEPObjectParserTest {

	private TlvHandlerRegistry tlvRegistry;

	@Before
	public void setUp() throws Exception {
		this.tlvRegistry = ServiceLoaderPCEPExtensionProviderContext.create().getTlvHandlerRegistry();
	}

	@Test
	public void testOpenObjectWithTLV() throws PCEPDeserializerException, IOException {
		final PCEPOpenObjectParser parser = new PCEPOpenObjectParser(this.tlvRegistry);
		final byte[] result = ByteArray.fileToBytes("src/test/resources/PCEPOpenObject1.bin");

		final OpenBuilder builder = new OpenBuilder();
		builder.setProcessingRule(false);
		builder.setIgnore(false);
		builder.setVersion(new ProtocolVersion((short) 1));
		builder.setKeepalive((short) 30);
		builder.setDeadTimer((short) 120);
		builder.setSessionId((short) 1);

		final Stateful tlv1 = new StatefulBuilder().setLspUpdateCapability(Boolean.TRUE).build();

		final Tlvs2Builder statBuilder = new Tlvs2Builder();
		statBuilder.setStateful(tlv1);

		builder.setTlvs(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.object.open.TlvsBuilder().addAugmentation(
				Tlvs2.class, statBuilder.build()).build());

		assertEquals(builder.build(), parser.parseObject(new ObjectHeaderImpl(false, false), ByteArray.cutBytes(result, 4)));
		assertArrayEquals(result, parser.serializeObject(builder.build()));
	}

	@Test
	public void testLspObjectWithTLV() throws IOException, PCEPDeserializerException {
		final PCEPLspObjectParser parser = new PCEPLspObjectParser(this.tlvRegistry);
		final byte[] result = ByteArray.fileToBytes("src/test/resources/PCEPLspObject1WithTLV.bin");

		final LspBuilder builder = new LspBuilder();
		builder.setProcessingRule(true);
		builder.setIgnore(true);
		builder.setAdministrative(true);
		builder.setDelegate(false);
		builder.setRemove(true);
		builder.setSync(false);
		builder.addAugmentation(Lsp1.class, new Lsp1Builder().setCreate(false).build());
		builder.setOperational(OperationalStatus.GoingDown);
		builder.setPlspId(new PlspId(0x12345L));

		final LspErrorCode tlv1 = new LspErrorCodeBuilder().setErrorCode(627610883L).build();
		final SymbolicPathName tlv2 = new SymbolicPathNameBuilder().setPathName(
				new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.SymbolicPathName("Med".getBytes())).build();
		builder.setTlvs(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.TlvsBuilder().setLspErrorCode(
				tlv1).setSymbolicPathName(tlv2).build());
		assertEquals(builder.build(), parser.parseObject(new ObjectHeaderImpl(true, true), ByteArray.cutBytes(result, 4)));
		assertArrayEquals(result, parser.serializeObject(builder.build()));
	}

	@Test
	public void testLspaObject() throws IOException, PCEPDeserializerException {
		final PCEPLspaObjectParser parser = new PCEPLspaObjectParser(this.tlvRegistry);
		final LspaBuilder builder = new LspaBuilder();
		final byte[] result = ByteArray.fileToBytes("src/test/resources/PCEPLspaObject3RandVals.bin");

		builder.setIgnore(true);
		builder.setProcessingRule(true);
		builder.setExcludeAny(new AttributeFilter(0x20A1FEE3L));
		builder.setIncludeAny(new AttributeFilter(0x1A025CC7L));
		builder.setIncludeAll(new AttributeFilter(0x2BB66532L));
		builder.setHoldPriority((short) 0x02);
		builder.setSetupPriority((short) 0x03);
		builder.setLocalProtectionDesired(true);

		assertEquals(builder.build(), parser.parseObject(new ObjectHeaderImpl(true, true), ByteArray.cutBytes(result, 4)));
		assertArrayEquals(result, parser.serializeObject(builder.build()));
	}

	@Test
	public void testSrpObject() throws IOException, PCEPDeserializerException {
		final PCEPSrpObjectParser parser = new PCEPSrpObjectParser(this.tlvRegistry);
		final byte[] result = new byte[] { (byte) 0x21, (byte) 0x10, (byte) 0x00, (byte) 0x0c, 0, 0, 0, (byte) 0x01, 0, 0, 0, (byte) 0x01 };

		final SrpBuilder builder = new SrpBuilder();
		builder.setProcessingRule(false);
		builder.setIgnore(false);
		builder.setOperationId(new SrpIdNumber(1L));
		builder.addAugmentation(Srp1.class, new Srp1Builder().setRemove(true).build());

		assertEquals(builder.build(), parser.parseObject(new ObjectHeaderImpl(false, false), ByteArray.cutBytes(result, 4)));
		assertArrayEquals(result, parser.serializeObject(builder.build()));
	}
}