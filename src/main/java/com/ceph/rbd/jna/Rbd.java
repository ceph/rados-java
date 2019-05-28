/*
 * RADOS Java - Java bindings for librados and librbd
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

package com.ceph.rbd.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Rbd extends Library {

    Rbd INSTANCE = (Rbd) Native.loadLibrary("rbd", Rbd.class);

    void rbd_version(IntByReference major, IntByReference minor, IntByReference extra);
    int rbd_create(Pointer io, String name, long size, IntByReference order);
    int rbd_create2(Pointer io, String name, long size, long features, IntByReference order);
    int rbd_create3(Pointer io, String name, long size, long features, IntByReference order, long stripe_unit, long stripe_count);
    int rbd_list(Pointer io, byte[] names, LongByReference size);
    int rbd_remove(Pointer io, String name);
    int rbd_rename(Pointer io, String srcname, String destname);
    int rbd_open_read_only(Pointer io, String name, Pointer image, String snap_name);
    int rbd_open(Pointer io, String name, Pointer image, String snap_name);
    int rbd_close(Pointer image);
    int rbd_stat(Pointer image, RbdImageInfo info, long infosize);
    int rbd_get_old_format(Pointer image, IntByReference old);
    int rbd_clone(Pointer p_io, String p_name, String p_snapname,
            Pointer c_io, String c_name, long features, IntByReference order);
    int rbd_clone2(Pointer p_io, String p_name, String p_snapname,
            Pointer c_io, String c_name, long features, IntByReference order,
            long stripe_unit, long stripe_count);
    int rbd_snap_create(Pointer image, String snapname);
    int rbd_snap_remove(Pointer image, String snapname);
    int rbd_snap_rollback(Pointer image, String snapname);
    int rbd_snap_protect(Pointer image, String snapname);
    int rbd_snap_unprotect(Pointer image, String snapname);
    int rbd_snap_is_protected(Pointer image, String snap_name, IntByReference is_protected);
    int rbd_snap_list(Pointer image, RbdSnapInfo[] snaps, IntByReference max_snaps);
    void rbd_snap_list_end(RbdSnapInfo[] snaps);
    int rbd_write(Pointer image, long offset, int len, byte[] buf);
    int rbd_read(Pointer image, long offset, int length, byte[] buffer);
    int rbd_copy2(Pointer source_image, Pointer dest_image);
    int rbd_resize(Pointer source_image, long size);
    int rbd_flatten(Pointer image);
    int rbd_snap_set(Pointer image, String snapname);
    int rbd_list_children(Pointer image, byte[] pools, LongByReference pools_len, byte[] images, LongByReference images_len);
}
