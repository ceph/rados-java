/*
 * RADOS Striper Java - Java bindings for librados
 *
 * Copyright (C) 2013 Wido den Hollander <wido@42on.com>
 *               2016 Arno Broekhof <arnobroekhof@gmail.com>
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
package com.ceph.radosstriper;


import static com.ceph.radosstriper.Library.rados;

import java.io.Closeable;
import java.util.concurrent.Callable;

import com.ceph.rados.RadosBase;
import com.ceph.rados.exceptions.RadosException;
import com.ceph.rados.jna.RadosObjectInfo;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;

public class IoCTXStriper extends RadosBase implements AutoCloseable {

    private static final int EXT_ATTR_MAX_LEN = 4096;

    private Pointer ioCtxStriperPtr;

    /**
     * Create a new IO Striper Context object
     * <p>
     * This constructor should never be called, IO Context
     * objects are created by the RADOS class and returned
     * when creating a IO Striper Context there
     */
    public IoCTXStriper(Pointer p) {
        this.ioCtxStriperPtr = p;
    }


    /**
     * Return the pointer to the IO Striper Context
     * <p>
     * This method is used internally and by the RADOS class
     * to destroy a IO Context
     *
     * @return Pointer
     */
    public Pointer getPointer() {
        return this.ioCtxStriperPtr.getPointer(0);
    }

    /**
     * Write to an object
     *
     * @param oid    The object to write to
     * @param buf    The content to write
     * @param length The length to write
     * @param offset The offset when writing
     * @throws RadosException
     */
    public void write(final String oid, final byte[] buf, final int length, final long offset) throws RadosException, IllegalArgumentException {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset shouldn't be a negative value");
        }
        if (buf.length < length) {
            throw new IllegalArgumentException("Length shouldn't be a smaller than Buffer length");
        }
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_write(getPointer(), oid, buf, length, offset);
            }
        }, "Failed writing %s bytes with offset %s to %s", length, offset, oid);
    }

    /**
     * Write to an object
     *
     * @param oid    The object to write to
     * @param buf    The content to write
     * @param offset The offset when writing
     * @throws RadosException
     */
    public void write(final String oid, final byte[] buf, final long offset) throws RadosException, IllegalArgumentException {
        this.write(oid, buf, buf.length, offset);
    }

    /**
     * Write to an object without an offset
     *
     * @param oid The object to write to
     * @param buf The content to write
     * @throws RadosException
     */
    public void write(String oid, byte[] buf) throws RadosException {
        this.writeFull(oid, buf, buf.length);
    }

    /**
     * Write an entire object
     * The object is filled with the provided data. If the object exists, it is atomically truncated and then written.
     *
     * @param oid The object to write to
     * @param buf The content to write
     * @param len The length of the data to write
     * @throws RadosException
     */
    public void writeFull(final String oid, final byte[] buf, final int len) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_write_full(getPointer(), oid, buf, len);
            }
        }, "Failed to write %s bytes to %s", len, oid);
    }

    /**
     * Sets the object layout's stripe unit of a rados striper for future objects.
     * This layout will be used when new objects are created (by writing to them)
     * Already existing objects will be opened with their own layout.
     *
     * @param stripeUnit the stripe_unit value of the new object layout
     * @returns 0 on success, negative error code on failure
     */
    public int setStripeUnit(final int stripeUnit) throws RadosException {
        return handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_set_object_layout_stripe_unit(getPointer(), stripeUnit);
            }
        }, "Failed to set stripe unit to: %s", stripeUnit);
    }

    /**
     * Sets the object layout's stripe count of a rados striper for future objects.
     * This layout will be used when new objects are created (by writing to them)
     * Already existing objects will be opened with their own layout.
     *
     * @param stripeCount the stripe_count value of the new object layout
     * @returns 0 on success, negative error code on failure
     */
    public int setStripeCount(final int stripeCount) throws RadosException {
        return handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_set_object_layout_stripe_count(getPointer(), stripeCount);
            }
        }, "Failed to set stripe count to: %s", stripeCount);
    }

    /**
     * Sets the object layout's object_size of a rados striper for future objects.
     * This layout will be used when new objects are created (by writing to them)
     * Already existing objects will be opened with their own layout.
     *
     * @param stripeObjectSize the targetted striper
     * @returns 0 on success, negative error code on failure
     */
    public int setStripeObjectSize(final int stripeObjectSize) throws RadosException {
        return handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_set_object_layout_object_size(getPointer(), stripeObjectSize);
            }
        }, "Failed to set stripe object size to: %s", stripeObjectSize);
    }

    /**
     * Write to an object without an offset
     *
     * @param oid    The object to write to
     * @param buf    The content to write
     * @param offset The offset when writing
     * @throws RadosException
     */
    public void write(String oid, String buf, long offset) throws RadosException {
        this.write(oid, buf.getBytes(), offset);
    }

    /**
     * Write to an object without an offset
     *
     * @param oid The object to write to
     * @param buf The content to write
     * @throws RadosException
     */
    public void write(String oid, String buf) throws RadosException {
        this.write(oid, buf.getBytes());
    }

    /**
     * Read data from an object
     *
     * @param oid    The object's name
     * @param length Amount of bytes to read
     * @param offset The offset where to start reading
     * @param buf    The buffer to store the result
     * @return Number of bytes read or negative on error
     * @throws RadosException
     */
    public int read(final String oid, final int length, final long offset, final byte[] buf)
            throws RadosException {
        if (length < 0) {
            throw new IllegalArgumentException("Length shouldn't be a negative value");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset shouldn't be a negative value");
        }

        return handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_read(getPointer(), oid, buf, length, offset);
            }
        }, "Failed to read object %s using offset %s and length %s", oid, offset, length);
    }

    /**
     * Resize an object
     *
     * @param oid  The object to resize
     * @param size The new length of the object.  If this enlarges the object,
     *             the new area is logically filled with
     *             zeroes. If this shrinks the object, the excess data is removed.
     * @throws RadosException
     */
    public void truncate(final String oid, final long size) throws RadosException {
        if (size < 0) {
            throw new IllegalArgumentException("Size shouldn't be a negative value");
        }
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_trunc(getPointer(), oid, size);
            }
        }, "Failed resizing objects %s to %s bytes", oid, size);
    }

    /**
     * @param oid The name to append to
     * @param buf The data to append
     * @param len The number of bytes to write from buf
     * @throws RadosException
     */
    public void append(final String oid, final byte[] buf, final int len) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_append(getPointer(), oid, buf, len);
            }
        }, "Failed appending %s bytes to object %s", len, oid);
    }

    /**
     * Append data to an object
     *
     * @param oid The name to append to
     * @param buf The data to append
     * @throws RadosException
     */
    public void append(String oid, byte[] buf) throws RadosException {
        this.append(oid, buf, buf.length);
    }

    /**
     * Append data to an object
     *
     * @param oid The name to append to
     * @param buf The data to append
     * @throws RadosException
     */
    public void append(String oid, String buf) throws RadosException {
        this.append(oid, buf.getBytes());
    }

    /**
     * Remove an object
     *
     * @param oid The object to remove
     * @throws RadosException
     */
    public void remove(final String oid) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_remove(getPointer(), oid);
            }
        }, "Failed removing object %s", oid);
    }

    /**
     * Stat an object
     *
     * @param oid The name of the object
     * @return RadosObjectInfo
     * The size and mtime of the object
     * @throws RadosException
     */
    public RadosObjectInfo stat(final String oid) throws RadosException {
        final LongByReference size = new LongByReference();
        final LongByReference mtime = new LongByReference();
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_stat(getPointer(), oid, size, mtime);
            }
        }, "Failed performing a stat on object %s", oid);
        return new RadosObjectInfo(oid, size.getValue(), mtime.getValue());
    }

    /**
     * Get the value of an extended attribute on an object.
     *
     * @param oid       The name of the object
     * @param xattrName The name of the extended attribute
     * @return The value of the extended attribute
     * @throws RadosException on failure -- common error codes:
     *                        -34 (ERANGE)  :   value exceeds buffer
     *                        -61 (ENODATA) :   no such attribute
     */
    public String getExtendedAttribute(final String oid, final String xattrName) throws RadosException {
        final byte[] buf = new byte[EXT_ATTR_MAX_LEN];
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_getxattr(getPointer(), oid, xattrName, buf, buf.length);
            }
        }, "Failed to get extended attribute %s on %s", xattrName, oid);
        // else...
        return Native.toString(buf);
    }

    /**
     * Set an extended attribute on an object.
     *
     * @param oid       The name of the object
     * @param xattrName The name of the extended attribute
     * @param val       The value of the extended attribute
     * @throws IllegalArgumentException attribute value is too long
     * @throws RadosException           on failure
     */
    public void setExtendedAttribute(final String oid, final String xattrName, String val) throws IllegalArgumentException, RadosException {
        final byte[] buf = Native.toByteArray(val);
        if (buf.length > EXT_ATTR_MAX_LEN) {
            throw new IllegalArgumentException("Length of attribute value must not exceed " + EXT_ATTR_MAX_LEN);
        }
        // else...
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_setxattr(getPointer(), oid, xattrName, buf, buf.length);
            }
        }, "Failed to set extended attribute %s on %s", xattrName, oid);
    }

    /**
     * Delete an extended attribute from an object.
     *
     * @param oid       The name of the object
     * @param xattrName The name of the extended attribute
     * @throws RadosException on failure
     */
    public void removeExtendedAttribute(final String oid, final String xattrName) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_rmxattr(getPointer(), oid, xattrName);
            }
        }, "Failed to remove extended attribute %s from %s", xattrName, oid);
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     *
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     *
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     *
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        rados.rados_striper_destroy(getPointer());
    }
}
