FROM openresty/openresty:alpine

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

EXPOSE 9102 9202

ENV HOSTNAME holmes-engine-mgmt

ENV LANG C.UTF-8

#install java-1.8-openjdk

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

ENV JAVA_ALPINE_VERSION 8.191.12-r0

RUN set -x \
        && apk add --no-cache \
                openjdk8="$JAVA_ALPINE_VERSION" \
        && [ "$JAVA_HOME" = "$(docker-java-home)" ]

#install neccessary tools
RUN apk upgrade \
    && apk update \
    && apk add --no-cache curl \
    && apk add --no-cache wget \
    && apk add --no-cache postgresql-client=10.5-r0

#switch the user to holmes
RUN addgroup -S holmes && adduser -S -G holmes holmes

#add the backend package to the docker image
WORKDIR /home/holmes
ADD holmes-engine-d-standalone-*-linux64.tar.gz /home/holmes/
RUN chmod -R a+rw /home/holmes/
RUN chmod -R a+rw /var/log/
RUN chmod 755 /home/holmes/bin/*.sh

USER holmes

CMD ["sh", "/home/holmes/bin/run.sh"]

