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

import com.ceph.rbd.Rbd;
import com.ceph.rbd.RbdImage;
import com.ceph.rbd.jna.RbdImageInfo;
import com.ceph.rbd.jna.RbdSnapInfo;
import com.ceph.rbd.RbdException;
import com.ceph.rados.Rados;
import com.ceph.rados.RadosException;
import com.ceph.rados.IoCTX;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.lang.IllegalArgumentException;
import junit.framework.*;
import java.security.SecureRandom;
import java.math.BigInteger;

public final class TestRbd extends TestCase {

    /**
        All these variables can be overwritten, see the setUp() method
     */
    String configFile = "/etc/ceph/ceph.conf";
    String id = "admin";
    String pool = "rbd";

    /**
        This test reads it's configuration from the environment
        Possible variables:
        * RADOS_JAVA_ID
        * RADOS_JAVA_CONFIG_FILE
        * RADOS_JAVA_POOL
     */
    public void setUp() {
        if (System.getenv("RADOS_JAVA_CONFIG_FILE") != null) {
            this.configFile = System.getenv("RADOS_JAVA_CONFIG_FILE");
        }

        if (System.getenv("RADOS_JAVA_ID") != null) {
            this.id = System.getenv("RADOS_JAVA_ID");
        }

        if (System.getenv("RADOS_JAVA_POOL") != null) {
            this.pool = System.getenv("RADOS_JAVA_POOL");
        }
    }

    /**
        This test verifies if we can get the version out of librados
        It's currently hardcoded to expect at least 0.48.0
     */
    public void testGetVersion() {
        int[] version = Rbd.getVersion();
        assertTrue(version[0] >= 0);
        assertTrue(version[1] >= 1);
        assertTrue(version[2] >= 8);
    }

    public void testCreateListAndRemoveImage() {
        try {
            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            long imageSize = 10485760;
            String imageName = "testimage1";
            String newImageName = "testimage2";

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, imageSize);

            String[] images = rbd.list();
            assertTrue("There were no images in the pool", images.length > 0);

            rbd.rename(imageName, newImageName);

            RbdImage image = rbd.open(newImageName);
            RbdImageInfo info = image.stat();

            assertEquals("The size of the image didn't match", imageSize, info.size);

            rbd.close(image);

            rbd.remove(newImageName);

            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testCreateFormatOne() {
        try {
            String imageName = "imageformat1";
            long imageSize = 10485760;

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, imageSize);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the old (1) format", oldFormat);

            rbd.close(image);

            rbd.remove(imageName);
            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testCreateFormatTwo() {
        try {
            String imageName = "imageformat2";
            long imageSize = 10485760;

            // We only want layering and format 2
            int features = (1<<0);

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the new (2) format", !oldFormat);

            rbd.close(image);

            rbd.remove(imageName);
            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testCreateAndClone() {
        try {
            String imageName = "baseimage-" + System.currentTimeMillis();
            long imageSize = 10485760;
            String snapName = "mysnapshot";

            // We only want layering and format 2
            int features = (1<<0);

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the new (2) format", !oldFormat);

            image.snapCreate(snapName);
            image.snapProtect(snapName);

            List<RbdSnapInfo> snaps = image.snapList();
            assertEquals("There should only be one snapshot", 1, snaps.size());

            rbd.clone(imageName, snapName, io, imageName + "-child1", features, 0);

            rbd.remove(imageName + "-child1");

            boolean isProtected = image.snapIsProtected(snapName);
            assertTrue("The snapshot was not protected", isProtected);

            image.snapUnprotect(snapName);
            image.snapRemove(snapName);

            rbd.close(image);

            rbd.remove(imageName);
            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testSnapList() {
        try {
            String imageName = "baseimage-" + System.currentTimeMillis();
            long imageSize = 10485760;
            String snapName = "mysnapshot";

            // We only want layering and format 2
            int features = (1<<0);

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the new (2) format", !oldFormat);

            for (int i = 0; i < 10; i++) {
              image.snapCreate(snapName + "-" + i);
              image.snapProtect(snapName + "-" + i);
            }

            List<RbdSnapInfo> snaps = image.snapList();
            assertEquals("There should only be ten snapshots", 10, snaps.size());

            for (int i = 0; i < 10; i++) {
              image.snapUnprotect(snapName + "-" + i);
              image.snapRemove(snapName + "-" + i);
            }

            rbd.close(image);

            rbd.remove(imageName);
            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testCreateAndWriteAndRead() {
        try {
            String imageName = "imageforwritetest";
            long imageSize = 10485760;

            // We only want layering and format 2
            int features = (1<<0);

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            String buf = "ceph";

            // Write the initial data
            image.write(buf.getBytes());

            // Start writing after what we just wrote
            image.write(buf.getBytes(), buf.length(), buf.length());

            byte[] data = new byte[buf.length()];
            image.read(0, data, buf.length());
            assertEquals("Did din't get back what we wrote", new String(data), buf);

            rbd.close(image);

            rbd.remove(imageName);
            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testCopy() {
        try {
            String imageName1 = "imagecopy1";
            String imageName2 = "imagecopy2";
            long imageSize = 10485760;

            // We only want layering and format 2
            int features = (1<<0);

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName1, imageSize, features, 0);
            rbd.create(imageName2, imageSize, features, 0);

            RbdImage image1 = rbd.open(imageName1);
            RbdImage image2 = rbd.open(imageName2);

            SecureRandom random = new SecureRandom();
            String buf = new BigInteger(130, random).toString(32);
            image1.write(buf.getBytes());

            rbd.copy(image1, image2);

            byte[] data = new byte[buf.length()];
            long bytes = image2.read(0, data, buf.length());
            assertEquals("The copy seem to have failed. The data we read didn't match", new String(data), buf);

            rbd.close(image1);
            rbd.close(image2);

            rbd.remove(imageName1);
            rbd.remove(imageName2);

            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    public void testResize() {
        try {
            String imageName = "imageforresizetest";
            long initialSize = 10485760;
            long newSize = initialSize * 2;

            // We only want layering and format 2
            int features = (1<<0);

            Rados r = new Rados(this.id);
            r.confReadFile(new File(this.configFile));
            r.connect();
            IoCTX io = r.ioCtxCreate(this.pool);

            Rbd rbd = new Rbd(io);
            rbd.create(imageName, initialSize, features, 0);
            RbdImage image = rbd.open(imageName);
            image.resize(newSize);
            RbdImageInfo info = image.stat();

            assertEquals("The new size of the image didn't match", newSize, info.size);

            rbd.close(image);

            rbd.remove(imageName);
            r.ioCtxDestroy(io);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } catch (RadosException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }
}
