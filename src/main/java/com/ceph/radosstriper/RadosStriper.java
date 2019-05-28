/*
 * RADOS Striper Java - Java bindings for librados
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
package com.ceph.radosstriper;


import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;
import com.ceph.rados.exceptions.RadosException;
import static com.ceph.radosstriper.Library.rados;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.util.concurrent.Callable;

public class RadosStriper extends Rados {
    private static final int EXT_ATTR_MAX_LEN = 4096;

    public RadosStriper(String id) {
        super(id);
    }

    public IoCTXStriper ioCtxCreateStriper(final IoCTX ioCTX) throws RadosException {
        final Pointer p = new Memory(Pointer.SIZE);
        handleReturnCode(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return rados.rados_striper_create(ioCTX.getPointer(), p);
            }
        }, "Failed to create the IoCTX Striper");
        return new IoCTXStriper(p);
    }

    public void destroy(IoCTXStriper ioCTXStriper) {
        rados.rados_striper_destroy(ioCTXStriper.getPointer());
    }
}
