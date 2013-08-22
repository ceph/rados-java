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

package com.ceph.rbd;

import com.ceph.rbd.jna.RbdImageInfo;
import com.ceph.rbd.jna.RbdSnapInfo;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.List;
import java.util.Arrays;

import static com.ceph.rbd.Library.rbd;
import com.sun.jna.NativeLong;

public class RbdImage {

    private Pointer image;
    private String name;

    public RbdImage(Pointer image, String name) {
        this.image = image;
        this.name = name;
    }

    /**
     * Returns the name of the image
     *
     * @return String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the pointer to the RBD image
     *
     * This method is used internally and by the RBD class
     * to close a RBD image
     *
     * @return Pointer
     */
    public Pointer getPointer() {
        return this.image.getPointer(0);
    }

    /**
     * Get information about a RBD image
     *
     * @return RbdImageInfo
     * @throws RbdException
     */
    public RbdImageInfo stat() throws RbdException {
        RbdImageInfo info = new RbdImageInfo();
        int r = rbd.rbd_stat(this.getPointer(), info, 0);
        if (r < 0) {
            throw new RbdException("Failed to stat the RBD image", r);
        }
        return info;
    }

    /**
     * Find out if the format of the RBD image is the old format
     * or not
     *
     * @return boolean
     * @throws RbdException
     */
    public boolean isOldFormat() throws RbdException {
        IntByReference old = new IntByReference();
        int r = rbd.rbd_get_old_format(this.getPointer(), old);
        if (r < 0) {
            throw new RbdException("Failed to get the RBD format", r);
        }

        if (old.getValue() == 1) {
            return true;
        }

        return false;
    }

    /**
     * Create a RBD snapshot
     *
     * @param snapName
     *        The name for the snapshot
     * @throws RbdException
     */
    public void snapCreate(String snapName) throws RbdException {
        int r = rbd.rbd_snap_create(this.getPointer(), snapName);
        if (r < 0) {
            throw new RbdException("Failed to create snapshot " + snapName, r);
        }
    }

    /**
     * Remove a RBD snapshot
     *
     * @param snapName
     *         The name of the snapshot
     * @throws RbdException
     */
    public void snapRemove(String snapName) throws RbdException {
        int r = rbd.rbd_snap_remove(this.getPointer(), snapName);
        if (r < 0) {
            throw new RbdException("Failed to remove snapshot " + snapName, r);
        }
    }

    /**
     * Protect a snapshot
     *
     * @param snapName
     *         The name of the snapshot
     * @throws RbdException
     */
    public void snapProtect(String snapName) throws RbdException {
        int r = rbd.rbd_snap_protect(this.getPointer(), snapName);
        if (r < 0) {
            throw new RbdException("Failed to protect snapshot " + snapName, r);
        }
    }

    /**
     * Unprotect a RBD snapshot
     *
     * @param snapName
     *         The name of the snapshot
     * @throws RbdException
     */
    public void snapUnprotect(String snapName) throws RbdException {
        int r = rbd.rbd_snap_unprotect(this.getPointer(), snapName);
        if (r < 0) {
            throw new RbdException("Failed to unprotect snapshot " + snapName, r);
        }
    }

    /**
     * Tells if a snapshot is protected or not
     *
     * @param snapname
     *         The name of the snapshot
     * @return boolean
     * @throws RbdException
     */
    public boolean snapIsProtected(String snapName) throws RbdException {
        IntByReference isProtected = new IntByReference();
        int r = rbd.rbd_snap_is_protected(this.getPointer(), snapName, isProtected);
        if (r < 0) {
            throw new RbdException("Failed to find out if snapshot " + snapName +  " is protected", r);
        }

        if (isProtected.getValue() == 1) {
            return true;
        }

        return false;
    }

    /**
     * List all snapshots
     *
     * @return List
     * @throws RbdException
     */
    public List<RbdSnapInfo> snapList() throws RbdException {
        IntByReference numSnaps = new IntByReference(16);
        RbdSnapInfo[] snaps;

        while (true) {
            snaps = new RbdSnapInfo[numSnaps.getValue()];
            int r = rbd.rbd_snap_list(this.getPointer(), snaps, numSnaps);
            if (r >= 0) {
                numSnaps.setValue(r);
                break;
            } else if (r == -34) { /* FIXME: hard-coded -ERANGE */
                numSnaps.setValue(r);
            } else {
                throw new RbdException("Failed listing snapshots", r);
            }
        }

        /*
         * Before clearing the backing list (well, just the name strings)
         * we'll disable auto sync so that junk doesn't repopulate the info
         * struct.
         *
         * FIXME: this is dumb, and I'm not exactly sure how to fix it, though
         * it does work. Note that this should be fine as long as you don't
         * re-use the RbdSnapInfo as a parameter to another native function.
         */
        for (int i = 0; i < numSnaps.getValue(); i++) {
          snaps[i].setAutoSynch(false);
        }
        
        rbd.rbd_snap_list_end(snaps);

        return Arrays.asList(snaps).subList(0, numSnaps.getValue());
    }

    /**
     * Write data to an RBD image
     *
     * @param data
     *         The to be written data
     * @param offset
     *         Where to start writing
     * @param length
     *         The number of bytes to write
     * @throws RbdException
     */
    public void write(byte[] data, long offset, int length) throws RbdException {
        if (length < 1) {
            throw new RbdException("There should be at least one byte to write");
        }

        int r = rbd.rbd_write(this.getPointer(), offset, length, data);
        if (r < 0) {
            throw new RbdException("Failed writing " + length + " bytes starting at offset " + offset, r);
        }
    }

    /**
     * Write data to an RBD image
     *
     * @param data
     *         The to be written data
     * @param offset
     *         Where to start writing
     */
    public void write(byte[] data, long offset) throws RbdException {
        this.write(data, offset, data.length);
    }

    /**
     * Write data to an RBD image
     *
     * @param data
     *         The to be written data
     */
    public void write(byte[] data) throws RbdException {
        this.write(data, 0, data.length);
    }

    /**
     * Read from an RBD image
     *
     * @param offset
     *         Where to start reading
     * @param buffer
     *         The buffer to store the result
     * @param length
     *         The amount of bytes to read
     * @return int
     *          The amount of bytes read
     */
    public int read(long offset, byte[] buffer, int length) {
        return rbd.rbd_read(this.getPointer(), offset, length, buffer);
    }

    /**
     * Resize an RBD image
     *
     * @param size
     *         The new size for the RBD image
     * @throws RbdException
     */
    public void resize(long size) throws RbdException {
        int r = rbd.rbd_resize(this.getPointer(), size);
        if (r < 0) {
            throw new RbdException("Failed to resize the RBD image", r);
        }
    }
}
