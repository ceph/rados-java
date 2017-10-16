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

package com.ceph.rados.jna;

import java.nio.ByteBuffer;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Rados extends Library {

    Rados INSTANCE = (Rados) Native.loadLibrary("rados", Rados.class);

    void rados_version(IntByReference major, IntByReference minor, IntByReference extra);
    int rados_create(PointerByReference cluster, String id);
    int rados_create2(PointerByReference cluster, String clustername, String name, long flags);
    int rados_conf_read_file(Pointer cluster, String path);
    int rados_conf_set(Pointer cluster, String option, String value);
    int rados_conf_get(Pointer cluster, String option, byte[] buf, int len);
    int rados_cluster_fsid(Pointer cluster, byte[] buf, int len);
    int rados_cluster_stat(Pointer cluster, RadosClusterInfo result);
    int rados_connect(Pointer cluster);
    int rados_pool_create(Pointer cluster, String name);
    int rados_pool_create_with_auid(Pointer cluster, String name, long auid);
    int rados_pool_create_with_all(Pointer cluster, String name, long auid, long crushrule);
    int rados_pool_create_with_crush_rule(Pointer cluster, String name, long crushrule);
    int rados_pool_delete(Pointer cluster, String name);
    int rados_pool_list(Pointer cluster, byte[] buf, int len);
    long rados_pool_lookup(Pointer cluster, String name);
    int rados_pool_reverse_lookup(Pointer cluster, long id, byte[] buf, long len);
    int rados_ioctx_pool_stat(Pointer ioctx, RadosPoolInfo result);
    long rados_get_instance_id(Pointer cluster);
    int rados_ioctx_create(Pointer cluster, String pool, Pointer ioctx);
    void rados_ioctx_destroy(Pointer ioctx);
    void rados_ioctx_set_namespace(Pointer ioctx, String namespace);
    long rados_ioctx_get_id(Pointer ioctx);
    int rados_ioctx_pool_set_auid(Pointer ioctx, long auid);
    int rados_ioctx_pool_get_auid(Pointer ioctx, LongByReference auid);
    int rados_ioctx_get_pool_name(Pointer ioctx, byte[] buf, int len);
    void rados_ioctx_locator_set_key(Pointer ioctx, String key);
    int rados_ioctx_snap_create(Pointer ioctx, String snapname);
    int rados_ioctx_snap_remove(Pointer ioctx, String snapname);
    int rados_ioctx_snap_lookup(Pointer ioctx, String snapname, LongByReference id);
    int rados_ioctx_snap_get_name(Pointer ioctx, long id, byte[] buf, long len);
    int rados_ioctx_snap_get_stamp(Pointer ioctx, long id, LongByReference time);
    int rados_ioctx_snap_list(Pointer ioctx, byte[] buf, int len);
    int rados_objects_list_open(Pointer ioctx, Pointer list);
    int rados_objects_list_next(Pointer list, Pointer entry, byte[] key);
    void rados_objects_list_close(Pointer list);
    int rados_write(Pointer ioctx, String oid, byte[] buf, int len, long off);
    int rados_write_full(Pointer ioctx, String oid, byte[] buf, int len);
    int rados_append(Pointer ioctx, String oid, byte[] buf, int len);
    int rados_read(Pointer ioctx, String oid, byte[] buf, int len, long off);
    int rados_remove(Pointer ioctx, String oid);
    int rados_trunc(Pointer ioctx, String oid, long size);
    int rados_clone_range(Pointer ioctx, String dst, long dst_off, String src, long src_off, long len);
    int rados_stat(Pointer ioctxo, String oi, LongByReference size, LongByReference mtime);
    Pointer rados_create_read_op();
    void rados_release_read_op(Pointer read_op);
    void rados_read_op_read(Pointer read_op, long offset, long len, ByteBuffer direct_buffer, LongByReference bytes_read, IntByReference prval);
    int rados_read_op_operate(Pointer read_op, Pointer ioctx, String oid, int flags);
    int rados_shutdown(Pointer cluster);

    // Asynchronous I/O
    int rados_aio_create_completion(Pointer callbackContext, Callback callbackComplete, Callback callbackSafe, PointerByReference completion);
    void rados_aio_release(Pointer completion);
    int rados_aio_flush(Pointer completion);
    int rados_aio_write(Pointer ioctx, String oid, Pointer completion, byte[] buffer, int length, long offset);
    int rados_aio_write_full(Pointer ioctx, String oid, Pointer completion, byte[] buffer, int length);
    int rados_aio_wait_for_complete(Pointer completion);
    int rados_aio_wait_for_safe(Pointer completion);
    
    // read, write, remove, iterate extended attributes
    int rados_getxattr(Pointer ioctx, String oid, String xattrName, byte[] buf, long len);
    int rados_setxattr(Pointer ioctx, String oid, String xattrName, byte[] buf, long len);
    int rados_rmxattr(Pointer ioctx, String oid, String xattrName);
    int rados_getxattrs(Pointer ioctx, String oid, Pointer iterator);
    int rados_getxattrs_next(Pointer iterator, PointerByReference attr_name, PointerByReference attr_value, IntByReference len);
    int rados_getxattrs_end(Pointer iterator);

    // Rados commands
    int rados_mon_command(Pointer cluster, String[] cmd, int cmdLen, String inbuf, int inbufLen, PointerByReference outBuf, IntByReference outBufLen, PointerByReference statusBuf, IntByReference statusBufLen);
    int rados_mon_command_target(Pointer cluster, String target, String[] cmd, int cmdLen, String inbuf, int inbufLen, PointerByReference outBuf, IntByReference outBufLen, PointerByReference statusBuf, IntByReference statusBufLen);
    int rados_osd_command(Pointer cluster, int osdId, String[] cmd, int cmdLen, String inbuf, int inbufLen, PointerByReference outBuf, IntByReference outBufLen, PointerByReference statusBuf, IntByReference statusBufLen);
}
