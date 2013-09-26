/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.framework;

import io.netty.channel.ChannelHandler;

import com.google.common.base.Preconditions;

public class ProtocolHandlerFactory<T> {
	private final ProtocolMessageEncoder<T> encoder;
	protected final ProtocolMessageFactory<T> msgFactory;

	public ProtocolHandlerFactory(final ProtocolMessageFactory<T> msgFactory) {
		this.msgFactory = Preconditions.checkNotNull(msgFactory);
		this.encoder = new ProtocolMessageEncoder<T>(msgFactory);
	}

	public ChannelHandler[] getEncoders() {
		return new ChannelHandler[] { this.encoder };
	}

	public ChannelHandler[] getDecoders() {
		return new ChannelHandler[] { new ProtocolMessageDecoder<T>(this.msgFactory) };
	}
}
