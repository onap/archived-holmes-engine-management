FROM ubuntu:16.04

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

EXPOSE 9102

#install openjdk-1.8
#RUN sed -i 's#http://archive.ubuntu.com#http://mirrors.163.com#g' /etc/apt/sources.list
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk wget

#configure the JDK
RUN sed -i 's|#networkaddress.cache.ttl=-1|networkaddress.cache.ttl=10|' /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $PATH:/usr/lib/jvm/java-8-openjdk-amd64/jre/bin:/usr/lib/jvm/java-8-openjdk-amd64/bin
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib
ENV JRE_HOME ${JAVA_HOME}/jre

#install ActiveMQ
RUN mkdir /home/downloads
RUN mkdir /home/activemq
RUN cd /home/downloads
RUN wget http://archive.apache.org/dist/activemq/apache-activemq/5.9.0/apache-activemq-5.9.0-bin.tar.gz
RUN tar -xzvf apache-activemq-5.9.0-bin.tar.gz -C /home/activemq/
RUN rm -rf /home/downloads

#add holmes related resources to the docker image
RUN mkdir /home/holmes
WORKDIR /home/holmes
ADD holmes-engine-d-standalone-*-linux64.tar.gz /home/holmes/
RUN chmod 755 /home/holmes/bin/*.sh


ENTRYPOINT /home/holmes/bin/run.sh
