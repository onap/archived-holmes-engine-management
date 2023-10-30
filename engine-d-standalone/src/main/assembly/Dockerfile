FROM onap/integration-java17:12.0.0

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

USER root

# 9102 - service port
# 9202 - debugging port
EXPOSE 9102 9202

ENV HOSTNAME=holmes-engine-mgmt \
    LANG=C.UTF-8 \
    PG_VERSION=12.2-r0

ADD holmes-engine-d-standalone-*-linux64.tar.gz /opt/onap/

RUN apk update \
    && apk add --no-cache curl postgresql-client \
    && chmod -R a+rw /opt/onap/ \
    && chmod -R a+rw /var/log/ \
    && chmod 755 /opt/onap/bin/*.sh

USER onap
ENTRYPOINT ["sh", "/opt/onap/bin/run.sh"]

