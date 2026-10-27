# RADOS Java

[![CI](https://github.com/ceph/rados-java/actions/workflows/ci.yml/badge.svg)](https://github.com/ceph/rados-java/actions/workflows/ci.yml)

Java bindings for [librados](https://docs.ceph.com/en/latest/rados/api/librados/),
[librbd](https://docs.ceph.com/en/latest/rbd/api/librbdpy/) and libradosstriper
from the [Ceph](https://ceph.io/) project, implemented with
[JNA](https://github.com/java-native-access/jna).

Because JNA binds at runtime there is no native build against the Ceph
headers: the bindings work on any system where JNA and the Ceph client
libraries are present.

## Requirements

* Java 17 or newer
* librados2, librbd1 and libradosstriper1 from Ceph **Nautilus (14.2) or
  newer**. The `rados_pool_stat_t` struct layout and the deprecated auid calls
  changed in Nautilus, older versions are not supported.

## Using the library

```xml
<dependency>
  <groupId>com.ceph</groupId>
  <artifactId>rados</artifactId>
  <version>0.7.0</version>
</dependency>
```

```java
import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;

try (Rados rados = new Rados("admin")) {
    rados.confReadFile(new File("/etc/ceph/ceph.conf"));
    rados.connect();

    try (IoCTX io = rados.ioCtxCreate("data")) {
        io.write("hello", "world".getBytes(StandardCharsets.UTF_8));
        System.out.println(io.stat("hello").getSize());
    }
}
```

`Rados`, `IoCTX`, `IoCTXStriper`, `RbdImage`, `Completion`, `ReadOp` and
`ListCtx` are `AutoCloseable` and release their native handles on `close()`.

## Building

Building requires JDK 17 or newer. The Maven wrapper downloads a suitable
Maven version.

```bash
./mvnw clean install -Dcom.ceph.rados.skipTests=true
```

## Tests

The unit tests that do not need a cluster can always be run:

```bash
./mvnw test -Dtest='RadosBaseTest,ErrorCodeTest' -Dsurefire.failIfNoSpecifiedTests=false
```

The remaining tests are integration tests that require a running Ceph cluster.
By default they read `/etc/ceph/ceph.conf`, authenticate as `admin` and use
the pools `data` (RADOS and striper) and `rbd` (RBD). These pools have to
EXIST prior to running the tests and should be EMPTY.

**DO NOT RUN THESE TESTS AGAINST A PRODUCTION CLUSTER.**

The defaults can be overridden with environment variables:

* `RADOS_JAVA_ID`
* `RADOS_JAVA_CONFIG_FILE`
* `RADOS_JAVA_POOL`

```bash
./mvnw test
./mvnw test -Dtest=com.ceph.rados.TestRados
./mvnw test -Dtest=com.ceph.rbd.TestRbd
```

`src/test/scripts/microceph-setup.sh` bootstraps a throw-away single-node
cluster with [MicroCeph](https://canonical-microceph.readthedocs-hosted.com/)
and creates the pools, which is what the GitHub Actions workflow uses.
