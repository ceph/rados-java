package com.ceph.radosstriper;


import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;

public class TestRadosStriper {
    private static String ENV_CONFIG_FILE = System.getenv("RADOS_JAVA_CONFIG_FILE");
    private static String ENV_ID = System.getenv("RADOS_JAVA_ID");
    private static String ENV_POOL = System.getenv("RADOS_JAVA_POOL");

    private static final String CONFIG_FILE = ENV_CONFIG_FILE == null ? "/etc/ceph/ceph.conf" : ENV_CONFIG_FILE;
    private static final String ID = ENV_ID == null ? "admin" : ENV_ID;
    private static final String POOL = ENV_POOL == null ? "data" : ENV_POOL;

    private static RadosStriper rados;
    private static IoCTXStriper ioctx;

    @BeforeAll
    public static void setUp() throws Exception {
        rados = new RadosStriper(ID);
        rados.confReadFile(new File(CONFIG_FILE));
        rados.connect();
        ioctx = rados.ioCtxCreateStriper(rados.ioCtxCreate(POOL));
    }

    /**
     * This test verifies if we can get the version out of librados
     * It's currently hardcoded to expect at least 0.48.0
     */
    @Test
    public void testGetVersion() {
        int[] version = Rados.getVersion();
        assertTrue(version[0] >= 0);
        assertTrue(version[1] >= 48);
        assertTrue(version[2] >= 0);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        rados.destroy(ioctx);
        rados.shutDown();


    }

    @Test
    public void testClusterFsid() throws Exception {
        assertNotNull(rados.clusterFsid(), "The fsid returned was null");
    }

    @Test
    public void testPoolList() throws Exception {
        String[] pools = rados.poolList();
        assertNotNull(pools);
        assertTrue(pools.length >= 1, "We expect at least 1 pool (the one using here)");
    }


    @Test
    public void testPoolLookup() throws Exception {
        long id = rados.poolLookup(POOL);
        assertTrue(id >= 0, "The POOL ID should be at least 0");

        String name = rados.poolReverseLookup(id);
        assertEquals(POOL, name, "The POOL names didn't match!");
    }

    @Test
    public void testInstanceId() throws Exception {
        long id = rados.getInstanceId();
        assertTrue(id > 0, "The id should be greater than 0");
    }

    /**
     * This is an pretty extensive test which creates an object
     * writes data, appends, truncates verifies the written data
     * and finally removes the object
     */
    @Test
    public void testIoCtxWriteListAndRead() throws Exception {
        /**
         * The object we will write to with the data
         */
        String oid = "rados-java";
        byte[] content = "junit wrote this".getBytes();

        ioctx.write(oid, content);

        /**
         * We simply append the already written data
         */
        ioctx.append(oid, content);
        assertEquals(content.length * 2, ioctx.stat(oid).getSize(), "The size doesn't match after the append");

        /**
         * We now resize the object to it's original size
         */
        ioctx.truncate(oid, content.length);
        assertEquals(content.length, ioctx.stat(oid).getSize(), "The size doesn't match after the truncate");

        ioctx.remove(oid);
    }

    /**
     * This test creates an object, appends some data and removes it afterwards
     */
    @Test
    public void testIoCtxStripedWriteAndAppendBytes() throws Exception {
        /**
         * The object we will write to with the data
         */
        String oid = "rados-java-striped";

        try {
            byte[] buffer = new byte[8192];
            // use a fix seed so that we always get the same data
            new Random(42).nextBytes(buffer);

            ioctx.write(oid, buffer);

            /**
             * We simply append the parts of the already written data
             */
            ioctx.append(oid, buffer, buffer.length / 2);

            int expectedFileSize = buffer.length + buffer.length / 2;
            assertEquals(expectedFileSize, ioctx.stat(oid).getSize(), "The size doesn't match after the append");

            byte[] readBuffer = new byte[expectedFileSize];
            ioctx.read(oid, expectedFileSize, 0, readBuffer);
            for (int i = 0; i < buffer.length; i++) {
                assertEquals(buffer[i], readBuffer[i]);
            }
            for (int i = 0; i < buffer.length / 2; i++) {
                assertEquals(buffer[i], readBuffer[i + buffer.length]);
            }
        } finally {
            ioctx.remove(oid);
        }
    }

    /**
     * This test tests write of partial buffer
     */
    @Test
    public void testIoCtxWritePartialBuffer() throws Exception {
        /**
         * The object we will write to with the data
         */
        String oid = "rados-java-partial";
        int write_len = 5;
        byte[] content = "junit wrote this".getBytes();

        ioctx.write(oid, content, write_len, 0);

        /**
         * We now check for expected size
         */
        assertEquals(write_len, ioctx.stat(oid).getSize(), "The size doesn't match length param");

        ioctx.remove(oid);
    }

}
