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

public class RadosObjectInfo {

    private String oid;
    private long size;
    private long mtime;

    public RadosObjectInfo(String oid, long size, long mtime) {
        this.oid = oid;
        this.size = size;
        this.mtime = mtime;
    }

    /**
     * Return the object name
     * @return String
    */
    public String getOid() {
        return this.oid;
    }

    /**
     * Returns the size in bytes
     * @return long
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Returns the modification time
     * @return long
     */
    public long getMtime() {
        return this.mtime;
    }

}
