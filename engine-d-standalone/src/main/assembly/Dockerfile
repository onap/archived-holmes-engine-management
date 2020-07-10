FROM onap/integration-java11:7.0.0

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

USER root

# 9102 - service port
# 9202 - debugging port
EXPOSE 9102 9202

ENV HOSTNAME=holmes-engine-mgmt \
    LANG=C.UTF-8

ADD holmes-engine-d-standalone-*-linux64.tar.gz /opt/onap/

RUN apt-get upgrade \
    && apt-get update \
    && apt-get install -y curl postgresql-client-11 \
    && chmod -R a+rw /opt/onap/ \
    && chmod -R a+rw /var/log/ \
    && chmod 755 /opt/onap/bin/*.sh

USER onap
ENTRYPOINT ["sh", "/opt/onap/bin/run.sh"]

