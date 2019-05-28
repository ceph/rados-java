# DOCKER-VERSION 1.0.0
#
# Ceph docker image for testing rados-java 
# based on the Demo Image by Sébastien Han "seb@redhat.com"
#
# VERSION 0.0.1

FROM ceph/base:tag-build-master-jewel-ubuntu-16.04
MAINTAINER Arno Broekhof "arnobroekhof@gmail.com"

# Add bootstrap script
ADD entrypoint.sh /entrypoint.sh

# Add OpenJDK 8
RUN apt-get update && \
    apt-get -y --no-install-recommends install openjdk-8-jdk libjna-java

# Add volumes for Ceph config and data
VOLUME ["/etc/ceph","/var/lib/ceph"]

# Expose the Ceph ports
EXPOSE 6789 6800 6801 6802 6803 6804 6805 80 5000

# Execute the entrypoint
ENTRYPOINT ["/entrypoint.sh"]
