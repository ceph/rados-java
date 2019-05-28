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
import com.ceph.rados.Rados;
import com.ceph.rados.exceptions.RadosException;
import com.ceph.rados.IoCTX;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.SecureRandom;
import java.math.BigInteger;

public final class TestRbd {
    /**
        This test reads it's configuration from the environment
        Possible variables:
        * RADOS_JAVA_ID
        * RADOS_JAVA_CONFIG_FILE
        * RADOS_JAVA_POOL
     */

    private static String ENV_CONFIG_FILE = System.getenv("RADOS_JAVA_CONFIG_FILE");
    private static String ENV_ID = System.getenv("RADOS_JAVA_ID");
    private static String ENV_POOL = System.getenv("RADOS_JAVA_POOL");

    private static final String CONFIG_FILE = ENV_CONFIG_FILE == null ? "/etc/ceph/ceph.conf" : ENV_CONFIG_FILE;
    private static final String ID = ENV_ID == null ? "admin" : ENV_ID;
    private static final String POOL = ENV_POOL == null ? "rbd" : ENV_POOL;

    private static Rados rados;
    private static IoCTX ioctx;

    private void cleanupImage(Rados r, IoCTX io, String image) throws RadosException, RbdException {
        if (r != null) {
            if (io != null) {
                Rbd rbd = new Rbd(ioctx);
                rbd.remove(image);
            }
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        rados = new Rados(ID);
        rados.confReadFile(new File(CONFIG_FILE));
        rados.connect();
        ioctx = rados.ioCtxCreate(POOL);
        String [] allOids = ioctx.listObjects();
        for (int i = 0; i < allOids.length; i++) {
            ioctx.remove(allOids[i]);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        rados.ioCtxDestroy(ioctx);
        rados.shutDown();
    }

    /**
        This test verifies if we can get the version out of librados
        It's currently hardcoded to expect at least 0.48.0
     */
    @Test
    public void testGetVersion() {
        int[] version = Rbd.getVersion();
        assertTrue(version[0] >= 0);
        assertTrue(version[1] >= 1);
        assertTrue(version[2] >= 8);
    }

    @Test
    public void testEmptyListImages() throws Exception {
        try {
            Rbd rbd = new Rbd(ioctx);

            List<String> imageList = Arrays.asList(rbd.list());
            assertTrue("There were more then 0 (" + imageList.size() + ") images in the pool", imageList.size() == 0);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    @Test
    public void testCreateListAndRemoveImage() throws Exception {
        long imageSize = 10485760;
        String imageName = "testimage1";
        String newImageName = "testimage2";

        try {
            Rbd rbd = new Rbd(ioctx);
            rbd.create(imageName, imageSize);

            String[] images = rbd.list();
            assertTrue("There were no images in the pool", images.length > 0);

            rbd.rename(imageName, newImageName);

            RbdImage image = rbd.open(newImageName);
            RbdImageInfo info = image.stat();

            assertEquals("The size of the image didn't match", imageSize, info.size);

            rbd.close(image);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } finally {
            cleanupImage(rados, ioctx, newImageName);
        }
    }

    @Test
    public void testCreateFormatOne() throws Exception {
        String imageName = "imageformat1";
        long imageSize = 10485760;

        try {
            Rbd rbd = new Rbd(ioctx);
            rbd.create(imageName, imageSize);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the old (1) format", oldFormat);

            rbd.close(image);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } finally {
            cleanupImage(rados, ioctx, imageName);
        }
    }

    @Test
    public void testCreateFormatTwo() throws Exception {
        String imageName = "imageformat2";
        long imageSize = 10485760;

        try {
            // We only want layering and format 2
            int features = (1<<0);

            Rbd rbd = new Rbd(ioctx);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the new (2) format", !oldFormat);

            rbd.close(image);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } finally {
            cleanupImage(rados, ioctx, imageName);
        }
    }

    @Test
    public void testCreateAndClone() throws Exception {
        String imageName = "baseimage-" + System.currentTimeMillis();
        String childImageName = imageName + "-child1";
        long imageSize = 10485760;
        String snapName = "mysnapshot";

        try {
            // We only want layering and format 2
            int features = (1<<0);

            Rbd rbd = new Rbd(ioctx);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the new (2) format", !oldFormat);

            image.snapCreate(snapName);
            image.snapProtect(snapName);

            List<RbdSnapInfo> snaps = image.snapList();
            assertEquals("There should only be one snapshot", 1, snaps.size());

            rbd.clone(imageName, snapName, ioctx, childImageName, features, 0);

            boolean isProtected = image.snapIsProtected(snapName);
            assertTrue("The snapshot was not protected", isProtected);

            rbd.remove(childImageName);

            image.snapUnprotect(snapName);
            image.snapRemove(snapName);

            rbd.close(image);

            rbd.remove (imageName);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    @Test
    public void testSnapList() throws Exception {
        String imageName = "baseimage-" + System.currentTimeMillis();
        long imageSize = 10485760;
        String snapName = "mysnapshot";
        int numSnaps = 32;
        int initialBufferSize = 16;

        try {
            // We only want layering and format 2
            int features = (1<<0);

            Rbd rbd = new Rbd(ioctx);
            rbd.create(imageName, imageSize, features, 0);

            RbdImage image = rbd.open(imageName);

            boolean oldFormat = image.isOldFormat();

            assertTrue("The image wasn't the new (2) format", !oldFormat);

            List<RbdSnapInfo> snapsEmpty = image.snapList(initialBufferSize);
            assertEquals("There should not be any snapshots for the image", 0, snapsEmpty.size());

            for (int i = 0; i < numSnaps; i++) {
                image.snapCreate(snapName + "-" + i);
                image.snapProtect(snapName + "-" + i);
            }

            List<RbdSnapInfo> snaps = image.snapList(initialBufferSize);
            assertEquals("The amount of snapshots does not match", numSnaps, snaps.size());

            for (int i = 0; i < numSnaps; i++) {
                image.snapUnprotect(snapName + "-" + i);
                image.snapRemove(snapName + "-" + i);
            }

            rbd.close(image);
            rbd.remove(imageName);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        }
    }

    @Test
    public void testCreateAndWriteAndRead() throws Exception {
        String imageName = "imageforwritetest";
        long imageSize = 10485760;

        try {
            // We only want layering and format 2
            int features = (1<<0);

            Rbd rbd = new Rbd(ioctx);
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
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } finally {
            cleanupImage(rados, ioctx, imageName);
        }
    }

    @Test
    public void testCopy() throws Exception {
        String imageName1 = "imagecopy1";
        String imageName2 = "imagecopy2";
        long imageSize = 10485760;

        try {
            // We only want layering and format 2
            int features = (1<<0);

            Rbd rbd = new Rbd(ioctx);
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
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } finally {
            cleanupImage(rados, ioctx, imageName1);
            cleanupImage(rados, ioctx, imageName2);
        }
    }

    @Test
    public void testResize() throws Exception {
        String imageName = "imageforresizetest";
        long initialSize = 10485760;
        long newSize = initialSize * 2;

        try {
            // We only want layering and format 2
            int features = (1<<0);

            Rbd rbd = new Rbd(ioctx);
            rbd.create(imageName, initialSize, features, 0);
            RbdImage image = rbd.open(imageName);
            image.resize(newSize);
            RbdImageInfo info = image.stat();

            assertEquals("The new size of the image didn't match", newSize, info.size);

            rbd.close(image);
        } catch (RbdException e) {
            fail(e.getMessage() + ": " + e.getReturnValue());
        } finally {
            cleanupImage(rados, ioctx, imageName);
        }
    }

  @Test
	public void testCloneAndFlatten() throws Exception {
    String parentImageName = "parentimage";
    String cloneImageName = "childimage";
    String snapName = "snapshot";
    long imageSize = 10485760;

		try {

			Rbd rbd = new Rbd(ioctx);

			// We only want layering and format 2
			int features = (1 << 0);

			// Create the parent image
			rbd.create(parentImageName, imageSize, features, 0);

			// Open the parent image
			RbdImage parentImage = rbd.open(parentImageName);

			// Verify that image is in format 2
			boolean oldFormat = parentImage.isOldFormat();
			assertTrue("The image wasn't the new (2) format", !oldFormat);

			// Create a snapshot on the parent image
			parentImage.snapCreate(snapName);

			// Verify that snapshot exists
			List<RbdSnapInfo> snaps = parentImage.snapList();
			assertEquals("There should only be one snapshot", 1, snaps.size());

			// Protect the snapshot
			parentImage.snapProtect(snapName);

			// Verify that snapshot is protected
			boolean isProtected = parentImage.snapIsProtected(snapName);
			assertTrue("The snapshot was not protected", isProtected);

			// Clone the parent image using the snapshot
			rbd.clone(parentImageName, snapName, ioctx, cloneImageName, features, 0);

			// Open the cloned image
			RbdImage cloneImage = rbd.open(cloneImageName);

			// Flatten the cloned image
			cloneImage.flatten();

			// Unprotect the snapshot, this will succeed only after the clone is flattened
			parentImage.snapUnprotect(snapName);

			// Verify that snapshot is not protected
			isProtected = parentImage.snapIsProtected(snapName);
			assertTrue("The snapshot was protected", !isProtected);

			// Delete the snapshot, this will succeed only after the clone is flattened and snapshot is unprotected
			parentImage.snapRemove(snapName);

			// Close both the parent and cloned images
			rbd.close(cloneImage);
			rbd.close(parentImage);
		} catch (RbdException e) {
			fail(e.getMessage() + ": " + e.getReturnValue());
		} finally {
        cleanupImage(rados, ioctx, parentImageName);
        cleanupImage(rados, ioctx, cloneImageName);
    }
	}

  @Test
	public void testListImages() throws Exception {
    String testImage = "testimage";
    long imageSize = 10485760;
    int imageCount = 3;

		try {
			Rbd rbd = new Rbd(ioctx);

			for (int i = 1; i <= imageCount; i++) {
				rbd.create(testImage + i, imageSize);
			}

			// List images without providing initial buffer size
			List<String> imageList = Arrays.asList(rbd.list());
			assertTrue("There were less than " + imageCount + " images in the pool", imageList.size() >= imageCount);

			for (int i = 1; i <= imageCount; i++) {
				assertTrue("Pool does not contain image testimage" + i, imageList.contains(testImage + i));
			}

			// List images and provide initial buffer size
			imageList = null;
			imageList = Arrays.asList(rbd.list(testImage.length()));
			assertTrue("There were less than " + imageCount + " images in the pool", imageList.size() >= imageCount);

			for (int i = 1; i <= imageCount; i++) {
				assertTrue("Pool does not contain image testimage" + i, imageList.contains(testImage + i));
			}
		} catch (RbdException e) {
			fail(e.getMessage() + ": " + e.getReturnValue());
		} finally {
      for (int i = 1; i <= imageCount; i++) {
        cleanupImage(rados, ioctx, testImage + i);
      }
    }
	}

  @Test
	public void testListChildren() throws Exception {
		try {
			Rbd rbd = new Rbd(ioctx);

			String parentImageName = "parentimage";
			String childImageName = "childImage";
			String snapName = "snapshot";
			long imageSize = 10485760;
			int childCount = 3;

			// We only want layering and format 2
			int features = (1 << 0);

			// Create the parent image
			rbd.create(parentImageName, imageSize, features, 0);

			// Open the parent image
			RbdImage parentImage = rbd.open(parentImageName);

			// Verify that image is in format 2
			boolean oldFormat = parentImage.isOldFormat();
			assertTrue("The image wasn't the new (2) format", !oldFormat);

			// Create a snapshot on the parent image
			parentImage.snapCreate(snapName);

			// Verify that snapshot exists
			List<RbdSnapInfo> snaps = parentImage.snapList();
			assertEquals("There should only be one snapshot", 1, snaps.size());

			// Protect the snapshot
			parentImage.snapProtect(snapName);

			// Verify that snapshot is protected
			boolean isProtected = parentImage.snapIsProtected(snapName);
			assertTrue("The snapshot was not protected", isProtected);

			for (int i = 1; i <= childCount; i++) {
				// Clone the parent image using the snapshot
				rbd.clone(parentImageName, snapName, ioctx, childImageName + i, features, 0);
			}

			// List the children of snapshot
			List<String> children = parentImage.listChildren(snapName);

			// Verify that two children are returned and the list contains their names
			assertEquals("Snapshot should have " + childCount + " children", childCount, children.size());

			for (int i = 1; i <= childCount; i++) {
				assertTrue(POOL + '/' + childImageName + i + " should be listed as a child", children.contains(POOL + '/' + childImageName + i));
			}

			// Delete the cloned images
			for (int i = 1; i <= childCount; i++) {
				rbd.remove(childImageName + i);
			}

			// Unprotect the snapshot, this will succeed only after the clone is flattened
			parentImage.snapUnprotect(snapName);

			// Verify that snapshot is not protected
			isProtected = parentImage.snapIsProtected(snapName);
			assertTrue("The snapshot was protected", !isProtected);

			// Delete the snapshot
			parentImage.snapRemove(snapName);

			// Close the parent imgag
			rbd.close(parentImage);

			// Delete the parent image
			rbd.remove(parentImageName);
		} catch (RbdException e) {
			fail(e.getMessage() + ": " + e.getReturnValue());
		}
	}
}
