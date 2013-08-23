/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Promise;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.protocol.framework.AbstractSessionNegotiator;
import org.opendaylight.protocol.framework.SessionListenerFactory;
import org.opendaylight.protocol.framework.SessionNegotiator;
import org.opendaylight.protocol.framework.SessionNegotiatorFactory;
import org.opendaylight.protocol.pcep.PCEPMessage;
import org.opendaylight.protocol.pcep.PCEPSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.UnsignedBytes;

/**
 * SessionNegotiator which takes care of making sure sessions between PCEP
 * peers are kept unique. This needs to be further subclassed to provide
 * either a client or server factory.
 */
public abstract class AbstractPCEPSessionNegotiatorFactory implements SessionNegotiatorFactory<PCEPMessage, PCEPSessionImpl, PCEPSessionListener> {
	private static final Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();
	private static final Logger logger = LoggerFactory.getLogger(AbstractPCEPSessionNegotiatorFactory.class);
	private final BiMap<byte[], Closeable> sessions = HashBiMap.create();
	private final Map<byte[], Short> sessionIds = new WeakHashMap<>();

	/**
	 * Create a new negotiator. This method needs to be implemented by
	 * subclasses to actually provide a negotiator.
	 * 
	 * @param promise Session promise to be completed by the negotiator
	 * @param channel Associated channel
	 * @param sessionId Session ID assigned to the resulting session
	 * @return a PCEP session negotiator
	 */
	protected abstract AbstractPCEPSessionNegotiator createNegotiator(Promise<PCEPSessionImpl> promise, PCEPSessionListener listener,
			Channel channel, short sessionId);

	@Override
	public final SessionNegotiator<PCEPSessionImpl> getSessionNegotiator(final SessionListenerFactory<PCEPSessionListener> factory,
			final Channel channel, final Promise<PCEPSessionImpl> promise) {

		final Object lock = this;

		logger.debug("Instantiating bootstrap negotiator for channel {}", channel);
		return new AbstractSessionNegotiator<PCEPMessage, PCEPSessionImpl>(promise, channel) {
			@Override
			protected void startNegotiation() throws Exception {
				logger.debug("Bootstrap negotiation for channel {} started", channel);

				/*
				 * We have a chance to see if there's a client session already
				 * registered for this client.
				 */
				final byte[] clientAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getAddress();

				synchronized (lock) {

					if (sessions.containsKey(clientAddress)) {
						// FIXME: cross-reference this to RFC5440

						final byte[] serverAddress = ((InetSocketAddress) channel.localAddress()).getAddress().getAddress();
						if (comparator.compare(serverAddress, clientAddress) > 0) {
							final Closeable n = sessions.remove(clientAddress);
							try {
								n.close();
							} catch (IOException e) {
								logger.warn("Unexpected failure to close old session", e);
							}
						} else {
							negotiationFailed(new RuntimeException("A conflicting session for address " +
									((InetSocketAddress) channel.remoteAddress()).getAddress() + " found."));
							return;
						}
					}

					final short sessionId = nextSession(clientAddress);
					final AbstractPCEPSessionNegotiator n = createNegotiator(promise, factory.getSessionListener(), channel, sessionId);

					sessions.put(clientAddress, new Closeable() {
						@Override
						public void close() {
							channel.close();
						}});

					channel.closeFuture().addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(final ChannelFuture future) throws Exception {
							synchronized (lock) {
								sessions.inverse().remove(this);
							}
						}
					});

					logger.debug("Replacing bootstrap negotiator for channel {}", channel);
					channel.pipeline().replace(this, "negotiator", n);
					n.startNegotiation();
				}
			}

			@Override
			protected void handleMessage(final PCEPMessage msg) throws Exception {
				throw new IllegalStateException("Bootstrap negotiator should have been replaced");
			}
		};
	}

	@GuardedBy("this")
	private short nextSession(final byte[] clientAddress) {
		/*
		 * FIXME: Improve the allocation algorithm to make sure:
		 * - no duplicate IDs are assigned
		 * - we retain former session IDs for a reasonable time
		 */
		Short next = sessionIds.get(clientAddress);
		if (next == null) {
			next = 0;
		}

		sessionIds.put(clientAddress, (short)((next + 1) % 255));
		return next;
	}
}
