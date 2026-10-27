#!/bin/bash
#
# Bootstrap a throw-away single-node Ceph cluster with MicroCeph and create
# the pools the rados-java tests expect ("data" for RADOS and the striper,
# "rbd" for RBD). Used by the GitHub Actions workflow, but also works on any
# Ubuntu machine with snapd. Must run as root.
#
# DO NOT run this on a machine that already runs Ceph.
#
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

OSD_COUNT=${OSD_COUNT:-3}
OSD_SIZE=${OSD_SIZE:-4G}
POOLS=(data rbd)

snap install microceph
snap refresh --hold microceph

microceph cluster bootstrap
microceph disk add "loop,${OSD_SIZE},${OSD_COUNT}"

# Expose the configuration and admin keyring to librados outside the snap.
mkdir -p /etc/ceph
cp /var/snap/microceph/current/conf/ceph.conf /etc/ceph/ceph.conf
for keyring in ceph.keyring ceph.client.admin.keyring; do
    if [ -f "/var/snap/microceph/current/conf/${keyring}" ]; then
        cp "/var/snap/microceph/current/conf/${keyring}" "/etc/ceph/${keyring}"
        chmod 0644 "/etc/ceph/${keyring}"
    fi
done

# Wait for all OSDs to come up before creating pools.
for _ in $(seq 1 60); do
    if microceph.ceph osd stat | grep -q "${OSD_COUNT} up"; then
        break
    fi
    sleep 5
done

for pool in "${POOLS[@]}"; do
    microceph.ceph osd pool create "${pool}" 8
done
microceph.ceph osd pool application enable data rados
microceph.ceph osd pool application enable rbd rbd

microceph.ceph -s
