#!/bin/bash

#
# Copyright 2017 ZTE Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

DIRNAME=`dirname $0`
RUNHOME=`cd $DIRNAME/; pwd`
echo @RUNHOME@ $RUNHOME

echo @JAVA_HOME@ $JAVA_HOME
JAVA="$JAVA_HOME/bin/java"
echo @JAVA@ $JAVA
main_path=$RUNHOME/..
cd $main_path
JAVA_OPTS="-Xms50m -Xmx128m"
port=8312
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$port,server=y,suspend=n"
echo @JAVA_OPTS@ $JAVA_OPTS

class_path="$main_path/:$main_path/holmes-engine-d.jar"
echo @class_path@ $class_path

sed -i "s/activemq.username=.*/activemq.username=activemq/" /home/activemq/apache-activemq-5.9.0/conf/credentials.properties
sed -i "s/activemq.password=.*/activemq.password=v1/" /home/activemq/apache-activemq-5.9.0/conf/credentials.properties
/home/activemq/apache-activemq-5.9.0/bin/activemq start

if [ -z ${JDBC_USERNAME} ]; then
    export JDBC_USERNAME=holmes
    echo "No user name is specified for the database. Use the default value \"$JDBC_USERNAME\"."
fi

if [ -z ${JDBC_PASSWORD} ]; then
    export JDBC_PASSWORD=holmespwd
    echo "No password is specified for the database. Use the default value \"$JDBC_PASSWORD\"."
fi

if [ -z ${DB_NAME} ]; then
    export DB_NAME=holmes
    echo "No database is name is specified. Use the default value \"$DB_NAME\"."
fi

sed -i "s|url:.*|url: jdbc:postgresql://$URL_JDBC/$DB_NAME|" "$main_path/conf/engine-d.yml"
sed -i "s|user:.*|user: $JDBC_USERNAME|" "$main_path/conf/engine-d.yml"
sed -i "s|password:.*|password: $JDBC_PASSWORD|" "$main_path/conf/engine-d.yml"

export SERVICE_IP=`hostname -i`
echo SERVICE_IP=${SERVICE_IP}

if [ ! -z ${TESTING} ] && [ ${TESTING} == 1 ]; then
    if [ ! -z ${HOST_IP} ]; then
        export HOSTNAME=${HOST_IP}:9102
    else
        export HOSTNAME=${SERVICE_IP}:9102
    fi
fi

#ActiveMQ IP Configurations
sed -i "s|brokerIp:.*|brokerIp: $SERVICE_IP|" "$main_path/conf/engine-d.yml"

KEY_PATH="$main_path/conf/holmes.keystore"
KEY_PASSWORD="holmes"

#HTTPS Configurations
sed -i "s|keyStorePath:.*|keyStorePath: $KEY_PATH|" "$main_path/conf/engine-d.yml"
sed -i "s|keyStorePassword:.*|keyStorePassword: $KEY_PASSWORD|" "$main_path/conf/engine-d.yml"

cat "$main_path/conf/engine-d.yml"


"$JAVA" $JAVA_OPTS -classpath "$class_path" org.onap.holmes.engine.EngineDActiveApp server "$main_path/conf/engine-d.yml"

