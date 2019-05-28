/*
 * RADOS Java - Java bindings for librados
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
 *
 * ***********
 * * history *
 * ***********
 * 2014-08-15 - initial implementation supporting ranged reads only
 */

package com.ceph.rados;

import static com.ceph.rados.Library.rados;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import com.ceph.rados.exceptions.RadosException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

public class ReadOp extends RadosBase implements AutoCloseable {

    public static class ReadResult {
        private final ByteBuffer buf;
        final LongByReference bytesread;
        final IntByReference rval;
        ReadResult(long buflen) throws RadosException {
            if ( buflen > Integer.MAX_VALUE ) {
                throw new RadosException("rados_read_op_read Java byte[] buffer cannot be longer than "+Integer.MAX_VALUE);
            }
            buf = ByteBuffer.allocateDirect((int) buflen);
            bytesread = new LongByReference();
            rval = new IntByReference();
        }
        public ByteBuffer getBuffer() { return buf; }
        public long getBytesRead() { return bytesread.getValue(); }
        public int  getRVal() { return rval.getValue(); }

        /**
         * Use this method if you do error handling with exceptions.
         *
         * Use it like the following:
         * ReadOp readOp = iocontext.readOpCreate();
         * ReadOp.ReadResult readResult = readOp.queueRead(offset, size);
         * ...
         * readOp.operate(name, flags);
         * readResult.raiseExceptionOnError();
         * // here you can be sure that the read operation has been successful
         *
         *
         * @param errorMsg the error message to use for the exception. Can be a format string
         * @param errorMsgArgs the arguments for the error message if the message is a format string
         * @throws RadosException Thrown if the result of the operation is not successful
         */
        public void raiseExceptionOnError(String errorMsg, Object... errorMsgArgs) throws RadosException {
            int returnCode = getRVal();
            if (returnCode < 0) {
                throwException(returnCode, String.format(errorMsg, errorMsgArgs));
            }
        }
    }

    private final Pointer ioctxPtr;
    private Pointer readOpPtr;

    /**
     * Create a new read_op object.
     *
     * This constructor should never be called, ReadOp
     * objects are created by the IoCTX class and returned
     * when creating a ReadOp there.
     */
    ReadOp(Pointer ioctx_p, Pointer readop_p) {
        this.ioctxPtr = ioctx_p;
        this.readOpPtr = readop_p;
    }

    Pointer getPointer() {
        return readOpPtr;
    }
    
    /**
     * Add a read operation to the rados_read_op_t via rados_read_op_read.  Note returned
     * ReadResult is not populated until after the operate() call.
     * 
     * @param offset starting offset into the object
     * @param len length of the read
     * @return Java object which will hold results of the requested read after operate() is called
     * @throws RadosException
     */
    public ReadResult queueRead(long offset, long len) throws RadosException {
        ReadResult r = new ReadResult(len);
        rados.rados_read_op_read(readOpPtr, offset, len, r.getBuffer(), r.bytesread, r.rval);
        return r;
    }

    /**
     * Executes operations added to the rados_read_op_t.
     * 
     * @param oid the name of the object to operate on
     * @param flags the flags for the operation
     * @return rados_read_op_operate return value
     * @see librados operation flags
     */
    public void operate(final String oid, final int flags) throws RadosException {
        handleReturnCode(new Callable<Number>() {
            @Override
            public Number call() throws Exception {
                return rados.rados_read_op_operate(readOpPtr, ioctxPtr, oid, flags);
            }
        }, "ReadOp.operate(%s, %d)", oid, flags);
    }

    @Override
    public void close() {
        if (readOpPtr != null) {
            rados.rados_release_read_op(readOpPtr);
            readOpPtr = null;
        }
    }
}