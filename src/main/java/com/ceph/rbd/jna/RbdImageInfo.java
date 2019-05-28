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

import com.sun.jna.Structure;
import java.util.List;
import java.util.Arrays;

public class RbdImageInfo extends Structure implements Structure.ByReference {
    public long size;
    public long obj_size;
    public long num_objs;
    public int order;
    public byte[] block_name_prefix;
    public long parent_pool;
    public byte[] parent_name;

    public RbdImageInfo() {
        super();
        this.block_name_prefix = new byte[24];
        this.parent_name = new byte[96];
    }

    protected List getFieldOrder() {
        return Arrays.asList("size", "obj_size", "num_objs", "order",
                             "block_name_prefix", "parent_pool",
                             "parent_name");
    }
}
