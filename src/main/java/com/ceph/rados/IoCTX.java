/*
 * RADOS Java - Java bindings for librados
 *
 * Copyright (C) 2013 Wido den Hollander <wido@42on.com>
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

import static com.ceph.rados.Library.rados;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.ceph.rados.exceptions.RadosException;
import com.ceph.rados.jna.RadosObjectInfo;
import com.ceph.rados.jna.RadosPoolInfo;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public class IoCTX extends RadosBase {

    private static final int    EXT_ATTR_MAX_LEN = 4096;

    private Pointer ioCtxPtr;

    /**
     * Create a new IO Context object
     *
     * This constructor should never be called, IO Context
     * objects are created by the RADOS class and returned
     * when creating a IO Context there
    */
    public IoCTX(Pointer p) {
        this.ioCtxPtr = p;
    }

    /**
     * Return the pointer to the IO Context
     *
     * This method is used internally and by the RADOS class
     * to destroy a IO Context
     *
     * @return Pointer
     */
    public Pointer getPointer() {
        return this.ioCtxPtr.getPointer(0);
    }

    /**
     * Get the pool ID of this context
     *
     * @return long
     */
    public long getId() {
        return rados.rados_ioctx_get_id(this.getPointer());
    }

    /**
     * Set the associated auid owner of the current pool
     *
     * @param auid
     *           The new auid
     * @throws RadosException
     */
    public void setAuid(final long auid) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_pool_set_auid(getPointer(), auid);
            }
        }, "Failed to set the auid to %s", auid);
    }

    /**
     * Get the associated auid owner of the current pool
     *
     * @return long
     * @throws RadosException
     */
    public long getAuid() throws RadosException {
        final LongByReference auid = new LongByReference();

        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_pool_get_auid(getPointer(), auid);
            }
        }, "Failed to get the auid");

        return auid.getValue();
    }

    /**
     * Get the pool name of the context
     *
     * @return String
     * @throws RadosException
     */
    public String getPoolName() throws RadosException {
        final byte[] buf = new byte[1024];
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_get_pool_name(getPointer(), buf, buf.length);
            }
        }, "Failed to get the pool name");
        return Native.toString(buf);
    }

    /**
     * Set the locator key
     *
     * @param key
     *          The new locator key or NULL to remove a previous one
     */
    public void locatorSetKey(String key) {
        rados.rados_ioctx_locator_set_key(this.getPointer(), key);
    }

    /**
     * List all objects in a pool
     *
     * @return String[]
     * @throws RadosException
     */
    public String[] listObjects() throws RadosException {
        Pointer entry = new Memory(Pointer.SIZE);
        List<String> objects = new ArrayList<String>();
        final Pointer list = new Memory(Pointer.SIZE);

        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_objects_list_open(getPointer(), list);
            }
        }, "Failed starting to list all objects");

        while (rados.rados_objects_list_next(list.getPointer(0), entry, null) == 0) {
            objects.add(entry.getPointer(0).getString(0));
        }

        rados.rados_objects_list_close(list.getPointer(0));

        return objects.toArray(new String[objects.size()]);
    }

    /**
     * List all objects in a pool by piece. Useful if a lot of objects are in the pool and do not
     * fit in memory through listObjects() method
     * @param limit
     * @return a ListCtx from which nextObjects()/nextObjects(skip) and getObjects() could be called
     * @throws RadosException
     */
    public ListCtx listObjectsPartial(int limit) throws RadosException {
        Pointer list = new Memory(Pointer.SIZE);

        int r = rados.rados_objects_list_open(this.getPointer(), list);
        if (r < 0) {
            throw new RadosException("Failed listing all objects", r);
        }
        return new ListCtx(limit, list);
    }

    /**
     * Write to an object
     *
     * @param oid
     *          The object to write to
     * @param buf
     *          The content to write
     * @param offset
     *          The offset when writing
     * @throws RadosException
     */
    public void write(final String oid, final byte[] buf, final long offset) throws RadosException, IllegalArgumentException {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset shouldn't be a negative value");
        }
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_write(getPointer(), oid, buf, buf.length, offset);
            }
        }, "Failed writing %s bytes with offset %s to %s", buf.length, offset, oid);
    }

    /**
     * Write to an object without an offset
     *
     * @param oid
     *          The object to write to
     * @param buf
     *          The content to write
     * @throws RadosException
     */
    public void write(String oid, byte[] buf) throws RadosException {
        this.writeFull(oid, buf, buf.length);
    }

    /**
     * Write an entire object
     * The object is filled with the provided data. If the object exists, it is atomically truncated and then written.
     *
     * @param oid
     *          The object to write to
     * @param buf
     *          The content to write
     * @param len
     *          The length of the data to write
     * @throws RadosException
     */
    public void writeFull(final String oid, final byte[] buf, final int len) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_write_full(getPointer(), oid, buf, len);
            }
        }, "Failed to write %s bytes to %s", len, oid);
    }

    /**
     * Write to an object without an offset
     *
     * @param oid
     *          The object to write to
     * @param buf
     *          The content to write
     * @param offset
     *          The offset when writing
     * @throws RadosException
     */
    public void write(String oid, String buf, long offset) throws RadosException {
        this.write(oid, buf.getBytes(), offset);
    }

    /**
     * Write to an object without an offset
     *
     * @param oid
     *          The object to write to
     * @param buf
     *          The content to write
     * @throws RadosException
     */
    public void write(String oid, String buf) throws RadosException {
        this.write(oid, buf.getBytes());
    }

    /**
     * Remove an object
     *
     * @param oid
     *          The object to remove
     * @throws RadosException
     */
    public void remove(final String oid) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_remove(getPointer(), oid);
            }
        }, "Failed removing object %s", oid);
    }

    /**
     * Read data from an object
     *
     * @param oid
     *          The object's name
     * @param length
     *          Amount of bytes to read
     * @param offset
     *          The offset where to start reading
     * @param buf
     *          The buffer to store the result
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
                return rados.rados_read(getPointer(), oid, buf, length, offset);
            }
        }, "Failed to read object %s using offset %s and length %s", oid, offset, length);
    }

    /**
     * Resize an object
     *
     * @param oid
     *           The object to resize
     * @param size
     *          The new length of the object.  If this enlarges the object,
     *          the new area is logically filled with
     *          zeroes. If this shrinks the object, the excess data is removed.
     * @throws RadosException
     */
    public void truncate(final String oid, final long size) throws RadosException {
        if (size < 0) {
            throw new IllegalArgumentException("Size shouldn't be a negative value");
        }
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_trunc(getPointer(), oid, size);
            }
        }, "Failed resizing objects %s to %s bytes", oid, size);
    }

    /**
     * Append data to an object
     *
     * @param oid
     *           The name to append to
     * @param buf
     *           The data to append
     * @throws RadosException
     */
    public void append(String oid, byte[] buf) throws RadosException {
        this.append(oid, buf, buf.length);
    }
    
    /**
     * 
     * @param oid
     *           The name to append to
     * @param buf
     *           The data to append
     * @param len
     *           The number of bytes to write from buf
     * @throws RadosException
     */
    public void append(final String oid, final byte[] buf, final int len) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_append(getPointer(), oid, buf, len);
            }
        }, "Failed appending %s bytes to object %s", len, oid);
    }

    /**
     * Append data to an object
     *
     * @param oid
     *           The name to append to
     * @param buf
     *           The data to append
     * @throws RadosException
     */
    public void append(String oid, String buf) throws RadosException {
        this.append(oid, buf.getBytes());
    }

    /**
    * Efficiently copy a portion of one object to another
    *
    * If the underlying filesystem on the OSD supports it, this will be a
    * copy-on-write clone.
    *
    * The src and dest objects must be in the same pg. To ensure this,
    * the io context should have a locator key set (see IoCTX.locatorSetKey()).
    *
    * @param dst
    *          The destination object
    * @param dst_off
    *          The offset at the destination object
    * @param src
    *          The source object
    * @param src_off
    *          The offset at the source object
    * @param len
    *          The amount of bytes to copy
    * @throws RadosException
    */
    public void clone(final String dst, final long dst_off, final String src, final long src_off, final long len) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_clone_range(getPointer(), dst, dst_off, src, src_off, len);
            }
        }, "Failed to copy %s bytes from %s to %s", len, src, dst);
    }

    /**
     * Stat an object
     *
     * @param oid
     *          The name of the object
     * @return RadosObjectInfo
     *           The size and mtime of the object
     * @throws RadosException
     */
    public RadosObjectInfo stat(final String oid) throws RadosException {
        final LongByReference size = new LongByReference();
        final LongByReference mtime = new LongByReference();
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_stat(getPointer(), oid, size, mtime);
            }
        }, "Failed performing a stat on object %s", oid);
        return new RadosObjectInfo(oid, size.getValue(), mtime.getValue());
    }

    /**
     * Stat the currently open pool
     *
     * @return RadosPoolInfo
     * @throws RadosException
     */
    public RadosPoolInfo poolStat() throws RadosException {
        final RadosPoolInfo result = new RadosPoolInfo();
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_pool_stat(getPointer(), result);
            }
        }, "Failed retrieving the pool stats");
        return result;
    }

    /**
     * Create a snapshot
     *
     * @param snapname
     *           The name of the snapshot
     * @throws RadosException
     */
    public void snapCreate(final String snapname) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_snap_create(getPointer(), snapname);
            }
        }, "Failed to create snapshot %s", snapname);
    }

    /**
     * Remove a snapshot
     *
     * @param snapname
     *           The name of the snapshot
     * @throws RadosException
     */
    public void snapRemove(final String snapname) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_snap_remove(getPointer(), snapname);
            }
        }, "Failed to remove snapshot %s", snapname);
    }

    /**
     * Get the ID of a snapshot
     *
     * @param snapname
     *            The name of the snapshot
     * @return long
     * @throws RadosException
     */
    public long snapLookup(final String snapname) throws RadosException {
        final LongByReference id = new LongByReference();
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_snap_lookup(getPointer(), snapname, id);
            }
        }, "Failed to lookup the ID of snapshot %s", snapname);
        return id.getValue();
    }

    /**
     * Get the name of a snapshot by it's ID
     *
     * @param id
     *          The ID of the snapshot
     * @return String
     * @throws RadosException
     */
    public String snapGetName(final long id) throws RadosException {
        final byte[] buf = new byte[512];
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_snap_get_name(getPointer(), id, buf, buf.length);
            }
        }, "Failed to lookup the name of snapshot %s", id);
        return new String(buf).trim();
    }

    /**
     * Get the timestamp of a snapshot
     *
     * @param id
     *         The ID of the snapshot
     * @return long
     * @throws RadosException
     */
    public long snapGetStamp(final long id) throws RadosException {
        final LongByReference time = new LongByReference();
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_snap_get_stamp(getPointer(), id, time);
            }
        }, "Failed to retrieve the timestamp of snapshot %s", id);
        return time.getValue();
    }

    /**
     * List all snapshots
     *
     * @return Long[]
     * @throws RadosException
     */
    public Long[] snapList() throws RadosException {
        final byte[] buf = new byte[512];

        final Integer result = handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_ioctx_snap_list(getPointer(), buf, buf.length);
            }
        }, "Failed to list all snapshots");

        Long[] snaps = new Long[result];
        for (int i = 0; i < result; i++) {
            snaps[i] = (long) buf[i];
        }
        return snaps;
    }

    public ReadOp readOpCreate() {
        return new ReadOp(getPointer(), rados.rados_create_read_op());
    }

    /**
     * Instead of releasing the read operation directly, better use ReadOp.close
     */
    @Deprecated
    public void readOpRelease(ReadOp read_op) {
        rados.rados_release_read_op(read_op.getPointer());
    }


    /**
     * Get the value of an extended attribute on an object.
     * 
     * @param oid
     *          The name of the object
     * @param xattrName
     *          The name of the extended attribute
     * @return
     * 		The value of the extended attribute
     * @throws RadosException
     * 		on failure -- common error codes:
     * 		-34 (ERANGE)  :	value exceeds buffer
     * 		-61 (ENODATA) :	no such attribute
     */
    public String getExtentedAttribute(final String oid, final String xattrName) throws RadosException {
        final byte[] buf = new byte[EXT_ATTR_MAX_LEN];
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_getxattr(getPointer(), oid, xattrName, buf, buf.length);
            }
        }, "Failed to get extended attribute %s on %s", xattrName, oid);
        // else...
        return  Native.toString(buf);
    }

    /**
     * Set an extended attribute on an object.
     * 
     * @param oid
     *          The name of the object
     * @param xattrName
     *          The name of the extended attribute
     * @param val
     * 		The value of the extended attribute
     * @throws IllegalArgumentException
     * 		attribute value is too long
     * @throws RadosException
     * 		on failure
     */
    public void setExtentedAttribute(final String oid, final String xattrName, String val) throws IllegalArgumentException, RadosException {
        final byte[] buf = Native.toByteArray(val);
        if (buf.length > EXT_ATTR_MAX_LEN) {
            throw new IllegalArgumentException( "Length of attribute value must not exceed " + EXT_ATTR_MAX_LEN);
        }
        // else...
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_setxattr(getPointer(), oid, xattrName, buf, buf.length);
            }
        }, "Failed to set extended attribute %s on %s", xattrName, oid);
    }

    /**
     * Delete an extended attribute from an object.
     * 
     * @param oid
     *          The name of the object
     * @param xattrName
     *          The name of the extended attribute
     * @throws RadosException
     * 		on failure
     */
    public void removeExtentedAttribute(final String oid, final String xattrName) throws RadosException {
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_rmxattr(getPointer(), oid, xattrName);
            }
        }, "Failed to remove extended attribute %s from %s", xattrName, oid);
   }

   /**
     * Get all extended attributes on an object.
     * 
     * @param oid
     *          The name of the object
     * @return
     * 		The map of the extended attributes
     * @throws RadosException
     * 		on failure
     */
    public Map<String, String> getExtentedAttributes(final String oid) throws RadosException {
        Map<String, String> attr_map = new HashMap<>();
        final Pointer iterator = new Memory(Pointer.SIZE);
        final PointerByReference attr_name = new PointerByReference();
        final PointerByReference attr_value = new PointerByReference();
        final IntByReference attr_value_len = new IntByReference();

        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_getxattrs(getPointer(), oid, iterator);
            }
        }, "Failed starting to list all extended attributes");

        while (rados.rados_getxattrs_next(iterator.getPointer(0), attr_name, attr_value, attr_value_len) == 0
                && attr_value_len.getValue() > 0) {
            int length = attr_value_len.getValue();
            String name = (attr_name.getValue() == null ? null : new String(attr_name.getValue().getString(0)));
            String value = (attr_value.getValue() == null ? null : new String(attr_value.getValue().getString(0)));
            if (length > 0 && name != null && value != null) {
                attr_map.put(name, value);
            }
        }

        rados.rados_getxattrs_end(iterator.getPointer(0));

        return attr_map;
    }
}
