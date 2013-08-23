/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.framework;

import java.util.List;

/**
 * Interface for factory for parsing and serializing protocol specific messages. Needs to be implemented by a protocol
 * specific message factory. The methods put/parse should delegate parsing to specific message parsers, e.g.
 * OpenMessageParser etc.
 *
 * @param <T> type of messages created by this factory
 */
public interface ProtocolMessageFactory<T extends ProtocolMessage> {

	/**
	 * Parses message from byte array. Requires specific protocol message header object to parse the header.
	 * 
	 * @param bytes byte array from which the message will be parsed
	 * @return List of specific protocol messages
	 * @throws DeserializerException if some parsing error occurs
	 * @throws DocumentedException if some documented error occurs
	 */
	public List<T> parse(final byte[] bytes) throws DeserializerException, DocumentedException;

	/**
	 * Serializes protocol specific message to byte array.
	 * 
	 * @param msg message to be serialized.
	 * @return byte array resulting message
	 */
	public byte[] put(final T msg);
}
