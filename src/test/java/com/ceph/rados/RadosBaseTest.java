/*
 * RADOS Java - Java bindings for librados
 *
 * Copyright (C) 2014 1&1 - Behar Veliqi <behar.veliqi@1und1.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.ceph.rados;

import com.ceph.rados.exceptions.RadosAlreadyConnectedException;
import com.ceph.rados.exceptions.RadosArgumentOutOfDomainException;
import com.ceph.rados.exceptions.RadosException;
import com.ceph.rados.exceptions.RadosInvalidArgumentException;
import com.ceph.rados.exceptions.RadosNotFoundException;
import com.ceph.rados.exceptions.RadosOperationInProgressException;
import com.ceph.rados.exceptions.RadosPermissionException;
import com.ceph.rados.exceptions.RadosReadOnlyException;
import com.ceph.rados.exceptions.RadosTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RadosBaseTest {

    private RadosBase radosBase;
    private Callable<Integer> callable;
    private String msg = "error message";

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        radosBase = new RadosBase();
        callable = mock(Callable.class);
    }

    @Test
    public void testReturnCodeZero() throws Exception {
        when(callable.call()).thenReturn(0);
        final Integer result = radosBase.handleReturnCode(callable, msg);

        assertEquals(result.intValue(), 0);
    }

    @Test
    public void testCallableThrowsGeneralException() throws Exception {
        assertThrows(RadosException.class, () -> {
            when(callable.call()).thenThrow(new Exception("fail"));

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus1() throws Exception {
        assertThrows(RadosPermissionException.class, () -> {
            when(callable.call()).thenReturn(-1);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus2() throws Exception {
        assertThrows(RadosNotFoundException.class, () -> {
            when(callable.call()).thenReturn(-2);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus22() throws Exception {
        assertThrows(RadosInvalidArgumentException.class, () -> {
            when(callable.call()).thenReturn(-22);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus30() throws Exception {
        assertThrows(RadosReadOnlyException.class, () -> {
            when(callable.call()).thenReturn(-30);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus33() throws Exception {
        assertThrows(RadosArgumentOutOfDomainException.class, () -> {
            when(callable.call()).thenReturn(-33);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus106() throws Exception {
        assertThrows(RadosAlreadyConnectedException.class, () -> {
            when(callable.call()).thenReturn(-106);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus110() throws Exception {
        assertThrows(RadosTimeoutException.class, () -> {
            when(callable.call()).thenReturn(-110);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testReturnCodeMinus115() throws Exception {
        assertThrows(RadosOperationInProgressException.class, () -> {
            when(callable.call()).thenReturn(-115);

            radosBase.handleReturnCode(callable, msg);
        });
    }

    @Test
    public void testUnhandledReturnCode() throws Exception {
        assertThrows(RadosException.class, () -> {
            when(callable.call()).thenReturn(-131);

            Exception ex = null;
            try {
                radosBase.handleReturnCode(callable, msg);
            } catch (Exception error) {
                ex = error;
                throw error;
            } finally {
                assert (ex != null);
                assertEquals(ex.getMessage(), msg + "; ENOTRECOVERABLE: State not recoverable (-131)");
            }

        });
    }

}
