/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.mode.impl.add.n.paths;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.protocol.bgp.mode.api.PathSelectionMode;

public class PathSelectionModeFactoryTest {
    @Test
    public void testCreateBestPathSelectionStrategy() throws Exception {
        final PathSelectionMode psm = new AddPathBestNPathSelection(2L);
        Assert.assertTrue(psm.createRouteEntry(true) instanceof ComplexRouteEntry);
        Assert.assertTrue(psm.createRouteEntry(false) instanceof SimpleRouteEntry);
    }
}