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

import com.sun.jna.Structure;
import java.util.List;
import java.util.Arrays;

public class RadosPoolInfo extends Structure {
    public long num_bytes;
    public long num_kb;
    public long num_objects;
    public long num_object_clones;
    public long num_object_copies;
    public long num_objects_missing_on_primary;
    public long num_objects_unfound;
    public long num_objects_degraded;
    public long num_rd;
    public long num_rd_kb;
    public long num_wr;
    public long num_wr_kb;

    protected List getFieldOrder() {
        return Arrays.asList("num_bytes", "num_kb", "num_objects", "num_object_clones",
                             "num_object_copies", "num_objects_missing_on_primary",
                             "num_objects_unfound", "num_objects_degraded",
                             "num_rd", "num_rd_kb", "num_wr", "num_wr_kb");
    }
}
