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
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;

public class RbdSnapInfo extends Structure {
    public long id;
    public long size;
    public String name;

    public RbdSnapInfo() {
        // Required for the toArray method
    }

    public RbdSnapInfo(Pointer p) {
        super(p);
    }

    protected List getFieldOrder() {
        return Arrays.asList("id", "size", "name");
    }
}
