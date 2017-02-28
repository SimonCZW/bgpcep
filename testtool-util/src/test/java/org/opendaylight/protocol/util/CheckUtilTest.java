/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.base.VerifyException;
import com.google.common.util.concurrent.CheckedFuture;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.protocol.util.CheckUtil.ListenerCheck;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CheckUtilTest {
    private final InstanceIdentifier<MockInterface> instanceIdentifier = InstanceIdentifier.create(MockInterface.class);
    @Mock
    private ChannelFuture future;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadOnlyTransaction readOnlyTransaction;
    @Mock
    private CheckedFuture checkedFuture;
    @Mock
    private Optional opt;
    @Mock
    private MockInterface mockInterface;
    @Mock
    private ListenerCheck listenerCheck;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(this.readOnlyTransaction).when(this.dataBroker).newReadOnlyTransaction();
        doReturn(this.checkedFuture).when(this.readOnlyTransaction).read(any(), any());
        doReturn(this.opt).when(this.checkedFuture).checkedGet();
        doReturn(this.mockInterface).when(this.opt).get();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrivateConstructor() throws Throwable {
        final Constructor<CheckUtil> c = CheckUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        try {
            c.newInstance();
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test(expected = VerifyException.class)
    public void waitFutureSuccessFail() throws Exception {
        when(this.future.isDone()).thenReturn(false);
        doReturn(this.future).when(this.future).addListener(any());
        CheckUtil.waitFutureSuccess(this.future);
    }

    @Test
    public void waitFutureSuccess() throws Exception {
        when(this.future.isSuccess()).thenReturn(true);
        doAnswer(invocation -> {
            invocation.getArgumentAt(0, GenericFutureListener.class).operationComplete(CheckUtilTest.this.future);
            return CheckUtilTest.this.future;
        }).when(this.future).addListener(any());
        CheckUtil.waitFutureSuccess(this.future);
    }

    @Test(expected = NullPointerException.class)
    public void readDataNull() throws Exception {
        doReturn(false).when(this.opt).isPresent();
        final InstanceIdentifier instanceIdentifier = null;
        CheckUtil.readData(this.dataBroker, instanceIdentifier, test -> false);
    }

    @Test(expected = AssertionError.class)
    public void readDataNotEquall() throws Exception {
        doReturn(true).when(this.opt).isPresent();
        doReturn(false).when(this.mockInterface).getResult();
        CheckUtil.readData(this.dataBroker, this.instanceIdentifier, test -> {
            assertTrue(test.getResult());
            return test;
        });
    }

    @Test(expected = AssertionError.class)
    public void checkNull() throws Exception {
        doReturn(true).when(this.opt).isPresent();
        CheckUtil.checkNull(this.dataBroker, this.instanceIdentifier);
    }

    @Test(expected = AssertionError.class)
    public void checkEquals() throws Exception {
        CheckUtil.checkEquals(()-> assertTrue(false));
    }

    @Test(expected = AssertionError.class)
    public void checkReceivedMessagesNotEqual() throws Exception {
        doReturn(0).when(this.listenerCheck).getListMessageSize();
        CheckUtil.checkReceivedMessages(this.listenerCheck, 1);
    }

    @Test
    public void checkReceivedMessagesEqual() throws Exception {
        doReturn(1).when(this.listenerCheck).getListMessageSize();
        CheckUtil.checkReceivedMessages(this.listenerCheck, 1);
    }

    private interface MockInterface extends DataObject {
        boolean getResult();
    }
}