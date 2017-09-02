FROM openresty/openresty:alpine

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

#install java-1.8-openjdk

ENV LANG C.UTF-8

# add a simple script that can auto-detect the appropriate JAVA_HOME value
# based on whether the JDK or only the JRE is installed
RUN { \
        echo '#!/bin/sh'; \
        echo 'set -e'; \
        echo; \
        echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
    } > /usr/local/bin/docker-java-home \
    && chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin

ENV JAVA_VERSION 8u131
ENV JAVA_ALPINE_VERSION 8.131.11-r2

RUN set -x \
    && apk add --no-cache \
        openjdk8="$JAVA_ALPINE_VERSION" \
    && [ "$JAVA_HOME" = "$(docker-java-home)" ]


#add activemq to the docker image
RUN mkdir /home/downloads
RUN mkdir /home/activemq
RUN cd /home/downloads
RUN apt-get update
RUN apt-get install -y wget
RUN wget http://archive.apache.org/dist/activemq/apache-activemq/5.9.0/apache-activemq-5.9.0-bin.tar.gz
RUN tar -xzvf apache-activemq-5.9.0-bin.tar.gz -C /home/activemq/
RUN rm -rf /home/downloads

#add holmes related resources to the docker image
RUN mkdir /home/holmes
WORKDIR /home/holmes
ADD holmes-engine-d-standalone-*-linux64.tar.gz /home/holmes/

RUN chmod 755 /home/holmes/bin/*.sh

ENTRYPOINT /home/holmes/bin/run.sh
