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

import com.ceph.rados.IoCTX;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import com.sun.jna.Native;

import static com.ceph.rbd.Library.rbd;

public class Rbd {

    Pointer io;


    /**
     * Get the librbd version
     *
     * @return a int array with the minor, major and extra version
     */
    public static int[] getVersion() {
        IntByReference minor = new IntByReference();
        IntByReference major = new IntByReference();
        IntByReference extra = new IntByReference();
        rbd.rbd_version(minor, major, extra);
        int[] returnValue = {minor.getValue(), major.getValue(), extra.getValue()};
        return returnValue;
    }

    public Rbd(IoCTX io) {
        this.io = io.getPointer();
    }

    /**
     * Create a new RBD image
     *
     * @param name
     *         The name of the new image
     * @param size
     *         The size of the new image in bytes
     * @param order
     *         Object/block size, as a power of two
     * @throws RbdException
     */
    public void create(String name, long size, int order) throws RbdException {
        IntByReference orderRef = new IntByReference(order);
        int r = rbd.rbd_create(this.io, name, size, orderRef);
        if (r < 0) {
            throw new RbdException("Failed to create image " + name, r);
        }
    }

    /**
     * Create a new RBD image
     *
     * @param name
     *         The name of the new image
     * @param size
     *         The size of the new image in bytes
     * @throws RbdException
     */
    public void create(String name, long size) throws RbdException {
        this.create(name, size, 0);
    }

    /**
     * Create a new RBD v2 image
     *
     * @param name
     *         The name of the new image
     * @param size
     *         The size of the new image in bytes
     * @param features
     *         Initial feature bits
     * @param order
     *         Object/block size, as a power of two
     * @throws RbdException
     */
    public void create(String name, long size, long features, int order) throws RbdException {
        IntByReference orderRef = new IntByReference(order);
        int r = rbd.rbd_create2(this.io, name, size, features, orderRef);
        if (r < 0) {
            throw new RbdException("Failed to create image " + name, r);
        }
    }

    /**
     * Create a new RBD v2 image
     *
     * @param name
     *         The name of the new image
     * @param size
     *         The size of the new image in bytes
     * @param features
     *         Initial feature bits
     * @throws RbdException
     */
    public void create(String name, long size, long features) throws RbdException {
        this.create(name, size, features, 0);
    }

    /**
     * Create a new RBD v2 image
     *
     * @param name
     *         The name of the new image
     * @param size
     *         The size of the new image in bytes
     * @param features
     *         Initial feature bits
     * @param order
     *         Object/block size, as a power of two
     * @param stripe_unit
     *         Stripe unit size, in bytes.
     * @param stripe_count
     *         Number of objects to stripe over before looping
     * @throws RbdException
     */
    public void create(String name, long size, long features, int order, long stripe_unit, long stripe_count) throws RbdException {
        IntByReference orderRef = new IntByReference(order);
        int r = rbd.rbd_create3(this.io, name, size, features, orderRef, stripe_unit, stripe_count);
        if (r < 0) {
            throw new RbdException("Failed to create image " + name, r);
        }
    }

    /**
     * Remove a RBD image
     *
     * @param name
     *         The name of the image
     * @throws RbdException
     */
    public void remove(String name) throws RbdException {
        int r = rbd.rbd_remove(this.io, name);
        if (r < 0) {
            throw new RbdException("Failed to remove image " + name, r);
        }
    }

    /**
     * Rename a RBD image
     *
     * @param srcName
     *         The source name
     * @param destName
     *         The new name for the image
     * @throws RbdException
     */
    public void rename(String srcName, String destName) throws RbdException {
        int r = rbd.rbd_rename(this.io, srcName, destName);
        if (r < 0) {
            throw new RbdException("Failed to rename image " + srcName + " to " + destName, r);
        }
    }

    /**
     * List all RBD images in this pool
     *
     * @return String[]
     * @throws RbdException
     */
	public String[] list() throws RbdException {
		int initialBufferSize = 1024;
		return list(initialBufferSize);
	}

	/**
	 * List all RBD images in this pool
	 *
	 * @param initialBufferSize
	 * 		   Initial size of the byte buffer holding image names
	 * @return String[]
	 *         Array of image names in the pool
	 * @throws RbdException
	 */
	public String[] list(int initialBufferSize) throws RbdException {
		LongByReference sizePointer = new LongByReference(initialBufferSize);
		byte[] names = new byte[initialBufferSize];

		int r = rbd.rbd_list(this.io, names, sizePointer);
		if (r < 0 && r != -34) {
			throw new RbdException("Failed to list RBD images", r);
		}

		// -34 (-ERANGE) is returned if the byte buffers are not big enough
		if (r == -34 || sizePointer.getValue() > initialBufferSize) {
			names = new byte[(int) sizePointer.getValue()];
			r = rbd.rbd_list(this.io, names, sizePointer);
			if (r < 0) {
				throw new RbdException("Failed to list RBD images", r);
			}
		}

		return new String(names).split("\0");
	}

    /**
     * Open a RBD image
     *
     * @param name
     *         The name of the image you want to open
     * @throws RbdException
     * @return RbdImage
     */
    public RbdImage open(String name) throws RbdException {
        return this.open(name, null);
    }

    /**
     * Open a RBD image with a specific snapshot
     *
     * @param name
     *         The name of the image you want to open
     * @param snapName
     *         The name of the snapshot to open
     * @throws RbdException
     * @return RbdImage
     */
    public RbdImage open(String name, String snapName) throws RbdException {
        Pointer p = new Memory(Pointer.SIZE);
        int r = rbd.rbd_open(this.io, name, p, snapName);
        if (r < 0) {
            throw new RbdException("Failed to open image " + name, r);
        }
        return new RbdImage(p, name);
    }

    /**
     * Open a RBD image read only
     *
     * @param name
     *         The name of the image you want to open
     * @throws RbdException
     * @return RbdImage
     */
    public RbdImage openReadOnly(String name) throws RbdException {
        return this.openReadOnly(name, null);
    }

    /**
     * Open a RBD image with a specific snapshot read only
     *
     * @param name
     *         The name of the image you want to open
     * @param snapName
     *         The name of the snapshot to open
     * @throws RbdException
     * @return RbdImage
     */
    public RbdImage openReadOnly(String name, String snapName) throws RbdException {
        Pointer p = new Memory(Pointer.SIZE);
        int r = rbd.rbd_open_read_only(this.io, name, p, snapName);
        if (r < 0) {
            throw new RbdException("Failed to open image " + name, r);
        }
        return new RbdImage(p, name);
    }

    /**
     * Close a RBD image
     *
     * @param image
     *         The RbdImage object
     * @throws RbdException
     */
    public void close(RbdImage image) throws RbdException {
        int r = rbd.rbd_close(image.getPointer());
        if (r < 0) {
            throw new RbdException("Failed to close image", r);
        }
    }

    /**
     * Clone a RBD image
     *
     * @param parentImage
     *         The name of the parent image
     * @param parentSnap
     *         The snapshot of the parent image (has to be protected)
     * @param childIo
     *         The IoCTX for the child image
     * @param childName
     *         The name for the child image
     * @param features
     *         The RBD features
     * @param order
     *         Object/block size, as a power of two
     * @param stripe_unit
     *         Stripe unit size, in bytes.
     * @param stripe_count
     *         Number of objects to stripe over before looping
     * @throws RbdException
     */
    public void clone(String parentImage, String parentSnap, IoCTX childIo,
                      String childName, long features, int order, long stripe_unit,
                      long stripe_count) throws RbdException {
        IntByReference orderRef = new IntByReference(order);
        int r = rbd.rbd_clone2(this.io, parentImage, parentSnap, childIo.getPointer(), childName, features, orderRef, stripe_unit, stripe_count);
        if (r < 0) {
            throw new RbdException("Failed to clone image " + parentImage + "@" + parentSnap + " to " + childName, r);
        }
    }

    /**
     * Clone a RBD image
     *
     * @param parentImage
     *         The name of the parent image
     * @param parentSnap
     *         The snapshot of the parent image (has to be protected)
     * @param childIo
     *         The IoCTX for the child image
     * @param childName
     *         The name for the child image
     * @param features
     *         The RBD features
     * @param order
     *         Object/block size, as a power of two
     * @throws RbdException
     */
    public void clone(String parentImage, String parentSnap, IoCTX childIo,
                      String childName, long features, int order) throws RbdException {
        IntByReference orderRef = new IntByReference(order);
        int r = rbd.rbd_clone(this.io, parentImage, parentSnap, childIo.getPointer(), childName, features, orderRef);
        if (r < 0) {
            throw new RbdException("Failed to clone image " + parentImage + "@" + parentSnap + " to " + childName, r);
        }
    }

    /**
     * Copy a RBD image
     *
     * @param sourceImage
     *         The source RbdImage
     * @param destImage
     *         The destination RbdImage
     * @throws RbdException
     */
    public void copy(RbdImage sourceImage, RbdImage destImage) throws RbdException {
        int r = rbd.rbd_copy2(sourceImage.getPointer(), destImage.getPointer());
        if (r < 0) {
            throw new RbdException("Failed to copy image " + sourceImage.getName() + " to " + destImage.getName(), r);
        }
    }
}
