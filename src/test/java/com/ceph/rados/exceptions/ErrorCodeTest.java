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
package com.ceph.rados.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorCodeTest {

    private static final int UNKNOWN_ERROR_CODE = -250;
    private static final int EPERM = -1;


    @Test
    public void testGetErrorName() throws Exception {
        final String errorName = ErrorCode.getErrorName(EPERM);

        assertEquals(errorName, "EPERM");
    }

    @Test
    public void testGetErrorNameUnknown() throws Exception {
        final String errorName = ErrorCode.getErrorName(UNKNOWN_ERROR_CODE);

        assertEquals(errorName, "UNKNOWN_ERROR");
    }

    @Test
    public void testgetErrorMessage() throws Exception {
        final String errorMessage = ErrorCode.getErrorMessage(EPERM);

        assertEquals(errorMessage, "Operation not permitted");
    }

    @Test
    public void testgetErrorMessageUnknown() throws Exception {
        final String errorMessage = ErrorCode.getErrorMessage(UNKNOWN_ERROR_CODE);

        assertEquals(errorMessage, "Unknown error code: " + UNKNOWN_ERROR_CODE);
    }

    @Test
    public void testEnum() throws Exception {
        final ErrorCode epermEnum = ErrorCode.getEnum(EPERM);

        assertEquals(epermEnum, ErrorCode.EPERM);
    }

    @Test
    public void testEnumUnknown() throws Exception {
        final ErrorCode epermEnum = ErrorCode.getEnum(UNKNOWN_ERROR_CODE);

        assertEquals(epermEnum, null);
    }
}
