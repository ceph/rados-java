#!/bin/bash
set -xe

# create data pool for testing
ceph osd pool create data 128 128

# sleep for 2 seconds to ensure the pool is created
sleep 2

cd /build
./mvnw clean test
