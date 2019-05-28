# RADOS Java
These are Java bindings for [librados](http://www.ceph.com/docs/master/rados/api/librados/) (C) which use JNA.

Both RADOS and RBD are implemented in these bindings.

By using JNA there is no need for building the bindings against any header
you can use them on any system where JNA and librados are present.

# Maven
## Building

```bash
$ mvn clean install (-Dcom.ceph.rados.skipTests)
```

## Tests

```bash
$ mvn test
```

You can also run specific tests:

```bash
mvn -Dtest=com.ceph.rados.TestRados
```

```bash
mvn -Dtest=com.ceph.rbd.TestRbd
```

# Unit Tests
The tests require a running Ceph cluster. By default it will read /etc/ceph/ceph.conf
and use "admin" as a cephx id.

All tests will be performed on the pools "data" (RADOS) and "rbd" (RBD).

These pools have to EXIST prior to running the tests and should be EMPTY!

DO NOT RUN THESE TESTS ON A PRODUCTION PLATFORM.

You can overrride this by setting these environment variables:
* RADOS_JAVA_ID
* RADOS_JAVA_CONFIG_FILE
* RADOS_JAVA_POOL

N.B.: You need to make sure jna.jar and junit.jar are present in /usr/share/java
