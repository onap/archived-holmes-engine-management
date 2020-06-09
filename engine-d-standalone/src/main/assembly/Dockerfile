FROM openresty/openresty:alpine

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

EXPOSE 9102 9202

ENV HOSTNAME=holmes-engine-mgmt \
    LANG=C.UTF-8 \
    JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk \
    PATH=$PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin \
    JAVA_ALPINE_VERSION=8.242.08-r0 \
    PG_VERSION=12.2-r0

#add the backend package to the docker image
WORKDIR /home/holmes
ADD holmes-engine-d-standalone-*-linux64.tar.gz /home/holmes/

# add a simple script that can auto-detect the appropriate JAVA_HOME value
# based on whether the JDK or only the JRE is installed
RUN { \
        echo '#!/bin/sh'; \
        echo 'set -e'; \
        echo; \
        echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
    } > /usr/local/bin/docker-java-home \
    && chmod +x /usr/local/bin/docker-java-home \
    && set -x \
    #install java-1.8-openjdk
    && apk add --no-cache openjdk8="$JAVA_ALPINE_VERSION" \
    && [ "$JAVA_HOME" = "$(docker-java-home)" ] \
    #install neccessary tools
    && apk upgrade \
    && apk update \
    && apk add --no-cache curl \
    && apk add --no-cache wget \
    && apk add --no-cache postgresql-client="$PG_VERSION" \
    && apk add --no-cache nss \

    #switch the user to holmes
    && addgroup -S holmes && adduser -S -G holmes holmes \

    && chmod -R a+rw /home/holmes/ \
    && chmod -R a+rw /var/log/ \
    && chmod 755 /home/holmes/bin/*.sh

USER holmes

CMD ["sh", "/home/holmes/bin/run.sh"]

