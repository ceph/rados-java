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

import com.ceph.rados.exceptions.ErrorCode;
import com.ceph.rados.exceptions.RadosAlreadyConnectedException;
import com.ceph.rados.exceptions.RadosArgumentOutOfDomainException;
import com.ceph.rados.exceptions.RadosException;
import com.ceph.rados.exceptions.RadosInvalidArgumentException;
import com.ceph.rados.exceptions.RadosNotFoundException;
import com.ceph.rados.exceptions.RadosOperationInProgressException;
import com.ceph.rados.exceptions.RadosPermissionException;
import com.ceph.rados.exceptions.RadosReadOnlyException;
import com.ceph.rados.exceptions.RadosTimeoutException;

import java.util.concurrent.Callable;

/**
 * Base class for doing all the exception handling.
 */
public class RadosBase {

    /**
     * @param callable to be called
     * @param errorMsg the error message to be used if any errors occur
     * @param errorMsgArgs the arguments for the error message
     * @param <T> the type of number for the return value (Integer, Long, ...)
     * @return the value returned by the callable
     * @throws RadosException if callable throws an exception or returns a negative value
     */
    final protected  <T extends Number> T handleReturnCode(Callable<T> callable, String errorMsg, Object... errorMsgArgs)
            throws RadosException {
        T result = call(callable);
        if (result.intValue() < 0) {
            throwException(result.intValue(), String.format(errorMsg, errorMsgArgs));
        }
        return result;
    }

    private <T> T call(Callable<T> callable) throws RadosException {
        T result;
        try {
            result = callable.call();
        } catch (Exception ex) {
            final String unknownErrorMsg = String.format("Unknown exception: %s: %s",
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw new RadosException(unknownErrorMsg, ex);
        }
        return result;
    }

    public static void throwException(int errorCode, String msg) throws RadosException {
        String exceptionMessage;
        ErrorCode errorCodeEnum = ErrorCode.getEnum(errorCode);
        if (errorCodeEnum != null) {
            exceptionMessage = String.format("%s; %s: %s", msg, errorCodeEnum.name(), errorCodeEnum.getErrorMessage());
            switch (errorCodeEnum) {
                case EPERM:
                    throw new RadosPermissionException(exceptionMessage, errorCode);
                case ENOENT:
                    throw new RadosNotFoundException(exceptionMessage, errorCode);
                case EINVAL:
                    throw new RadosInvalidArgumentException(exceptionMessage, errorCode);
                case EROFS:
                    throw new RadosReadOnlyException(exceptionMessage, errorCode);
                case EDOM:
                    throw new RadosArgumentOutOfDomainException(exceptionMessage, errorCode);
                case EISCONN:
                    throw new RadosAlreadyConnectedException(exceptionMessage, errorCode);
                case ETIMEDOUT:
                    throw new RadosTimeoutException(exceptionMessage, errorCode);
                case EINPROGRESS:
                    throw new RadosOperationInProgressException(exceptionMessage, errorCode);
            }
        } else {
            exceptionMessage = String.format("%s; error code: %d", msg, errorCode);
        }

        throw new RadosException(exceptionMessage, errorCode);
    }
}
