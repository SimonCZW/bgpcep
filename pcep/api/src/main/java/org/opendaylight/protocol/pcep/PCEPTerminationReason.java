/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep;

import org.opendaylight.protocol.framework.TerminationReason;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class PCEPTerminationReason implements TerminationReason {

	@Override
	public final String toString() {
		return addToStringAttributes(Objects.toStringHelper(this)).toString();
	}

	abstract protected ToStringHelper addToStringAttributes(ToStringHelper toStringHelper);
}
