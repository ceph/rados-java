/*
 * RADOS Java - Java bindings for librados
 *
 * Copyright (C) 2013 Wido den Hollander <wido@42on.com>
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

import java.io.IOException;

public class RadosException extends IOException {

    protected int returnValue;

    /**
     * Throw a a RadosException
     *
     * @param message
     *         The error message
     */
    public RadosException(String message) {
        super(message);
    }

    /**
     * Throw a a RadosException
     *
     * @param message
     *         The error message
     * @param returnValue
     *         The return value of the rados_ call
     */
    public RadosException(String message, int returnValue) {
        super(message + " (" + returnValue + ")");
        this.returnValue = returnValue;
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public RadosException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get the return value passed on to the constructor
     *
     * @return int
     */
    public int getReturnValue() {
        return this.returnValue;
    }

}
