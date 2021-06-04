#!/bin/sh

#
# Copyright 2017-2020 ZTE Corporation.
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
JAVA_OPTS="-Xms256m -Xmx1g"
port=9202
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=*:$port,server=y,suspend=n"
echo @JAVA_OPTS@ $JAVA_OPTS

class_path="$main_path/lib/*"
echo @class_path@ $class_path

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

# if deployed using helm, use the helm-generated configuration file.
if [ -d /opt/hemconfig ]; then
    cp /opt/hemconfig/engine-d.yml "$main_path/conf/engine-d.yml"
else
    sed -i "s|url:.*|url: jdbc:postgresql://$URL_JDBC/$DB_NAME|" "$main_path/conf/engine-d.yml"
    sed -i "s|user:.*|user: $JDBC_USERNAME|" "$main_path/conf/engine-d.yml"
    sed -i "s|password:.*|password: $JDBC_PASSWORD|" "$main_path/conf/engine-d.yml"
fi

export SERVICE_IP=`hostname -i | awk '{print $1}'`
echo SERVICE_IP=${SERVICE_IP}

if [ ! -z ${TESTING} ] && [ ${TESTING} = 1 ]; then
    if [ ! -z ${HOST_IP} ]; then
        export HOSTNAME=${HOST_IP}:9102
    else
        export HOSTNAME=${SERVICE_IP}:9102
    fi
fi

export DB_PORT=5432
if [ ! -z ${URL_JDBC} ] && [ `expr index $URL_JDBC :` != 0 ]; then
    export DB_PORT="${URL_JDBC##*:}"
fi
echo DB_PORT=$DB_PORT

if [ -z ${ENABLE_ENCRYPT} ]; then
    export ENABLE_ENCRYPT=true
fi
echo ENABLE_ENCRYPT=$ENABLE_ENCRYPT

KEY_PATH="/opt/onap/conf/holmes.keystore"
KEY_PASSWORD="holmes"

if [ -f "/opt/app/osaaf/local/org.onap.holmes-engine-mgmt.p12" ]; then
    KEY_PATH="/opt/app/osaaf/local/org.onap.holmes-engine-mgmt.p12"
    KEY_PASSWORD=`head -n1 "/opt/app/osaaf/local/mycreds.prop" | cut -d= -f2`
fi

echo "KEY_PATH=$KEY_PATH"
echo "KEY_PASS=$KEY_PASSWORD"

#HTTPS Configurations
sed -i "s|keyStorePath:.*|keyStorePath: $KEY_PATH|" "$main_path/conf/engine-d.yml"
sed -i "s|keyStorePassword:.*|keyStorePassword: $KEY_PASSWORD|" "$main_path/conf/engine-d.yml"

if [ "${ENABLE_ENCRYPT}"x = "true"x ]; then
    sed -i "s|type:\s*https\?$|type: https|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?keyStorePath|keyStorePath|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?keyStorePassword|keyStorePassword|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?validateCerts|validateCerts|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?validatePeers|validatePeers|" "$main_path/conf/engine-d.yml"
else
    sed -i 's|type:\s*https\?$|type: http|' "$main_path/conf/engine-d.yml"
    sed -i "s|#\?keyStorePath|#keyStorePath|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?keyStorePassword|#keyStorePassword|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?validateCerts|#validateCerts|" "$main_path/conf/engine-d.yml"
    sed -i "s|#\?validatePeers|#validatePeers|" "$main_path/conf/engine-d.yml"
fi

cat "$main_path/conf/engine-d.yml"

${RUNHOME}/initDB.sh "$JDBC_USERNAME" "$JDBC_PASSWORD" "$DB_NAME" "$DB_PORT" "${URL_JDBC%:*}"

"$JAVA" $JAVA_OPTS -classpath "$class_path" org.onap.holmes.engine.EngineDActiveApp server "$main_path/conf/engine-d.yml"

