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

package com.ceph.rados;

import java.util.Arrays;

import com.ceph.rados.exceptions.RadosException;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import static com.ceph.rados.Library.rados;

/**
 * Iterates over the objects in a pool in batches of at most {@code limit}
 * entries. Obtained via {@link IoCTX#listObjectsPartial(int)}.
 */
public class ListCtx implements AutoCloseable {
    private Pointer list;
    private final String[] ids;
    private int size;
    private final int limit;

    /**
     * @param limit maximum number of objects returned per call to nextObjects()
     * @param list  pointer to the rados_list_ctx_t handle
     */
    protected ListCtx(int limit, Pointer list) {
        this.limit = limit;
        this.ids = new String[limit];
        this.list = list;
        this.size = 0;
    }

    /**
     * List a subset of objects in a pool
     *
     * @return the number of ids get or 0 when the end of the list is reached
     * @throws RadosException
     */
    public int nextObjects() throws RadosException {
        return nextObjects(0);
    }

    /**
     * List a subset of objects in a pool skipping the first 'skip' objects
     *
     * @param skip number of objects to skip
     * @return the number of ids get or 0 when the end of the list is reached
     * @throws RadosException
     */
    public int nextObjects(long skip) throws RadosException {
        if (list == null) {
            return 0;
        }
        Pointer entry = new Memory(Native.POINTER_SIZE);
        long j = 0;
        while (j < skip && rados.rados_nobjects_list_next(list.getPointer(0), entry, null, null) == 0) {
            j++;
        }
        int i = 0;
        while (i < limit && rados.rados_nobjects_list_next(list.getPointer(0), entry, null, null) == 0) {
            ids[i] = entry.getPointer(0).getString(0, "UTF-8");
            i++;
        }
        if (i < limit) {
            // end of the listing reached
            close();
        }
        this.size = i;
        return this.size;
    }

    /**
     * @return the number of objects returned by the last call to nextObjects()
     */
    public int size() {
        if (list == null) {
            return 0;
        }
        return this.size;
    }

    /**
     * @return the objects returned by the last call to nextObjects()
     */
    public String[] getObjects() {
        return Arrays.copyOf(this.ids, this.size);
    }

    /**
     * Close the listing. Safe to call more than once.
     */
    @Override
    public void close() {
        if (list != null) {
            rados.rados_nobjects_list_close(list.getPointer(0));
            list = null;
        }
    }
}
