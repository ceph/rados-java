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
package com.ceph.radosstriper.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;

public interface RadosStriper extends Library {

    RadosStriper INSTANCE = (RadosStriper) Native.loadLibrary("radosstriper", RadosStriper.class);


    int rados_striper_create(Pointer ioctx, Pointer striper);

    void rados_striper_destroy(Pointer striper);

    int rados_set_object_layout_stripe_unit(Pointer striper, int stripe_unit);

    int rados_set_object_layout_stripe_count(Pointer striper, int stripe_count);

    int rados_set_object_layout_object_size(Pointer striper, int object_size);

    int rados_striper_write(Pointer striper, String oid, byte[] buf, int len, long off);

    int rados_striper_write_full(Pointer striper, String oid, byte[] buf, int len);

    int rados_striper_append(Pointer striper, String oid, byte[] buf, int len);

    int rados_striper_read(Pointer striper, String oid, byte[] buf, int len, long off);

    int rados_striper_remove(Pointer striper, String oid);

    int rados_striper_trunc(Pointer striper, String oid, long size);

    int rados_striper_getxattr(Pointer striper, String oid, String xattrName, byte[] buf, long len);

    int rados_striper_setxattr(Pointer striper, String oid, String xattrName, byte[] buf, long len);

    int rados_striper_rmxattr(Pointer striper, String oid, String xattrName);

    int rados_striper_stat(Pointer striper, String oi, LongByReference size, LongByReference mtime);

}
