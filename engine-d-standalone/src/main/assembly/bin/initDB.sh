#!/bin/sh
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
HOME=`cd $DIRNAME/; pwd`
user=$1
password=$2
dbname=$3
port=$4
host=$5
echo "Initializing the holmes engine management database..."
main_path=$HOME/..

sed -i "s|DBNAME|$dbname|g" "$main_path/dbscripts/postgresql/onap-holmes_engine-createobj.sql"
sed -i "s|DBUSER|$user|g" "$main_path/dbscripts/postgresql/onap-holmes_engine-createobj.sql"
sed -i "s|DBPWD|$password|g" "$main_path/dbscripts/postgresql/onap-holmes_engine-createobj.sql"

cat $main_path/dbscripts/postgresql/onap-holmes_engine-createobj.sql

echo "dbname=$dbname"
echo "user=$user"
echo "password=$password"
echo "port=$port"
echo "host=$host"

export PGPASSWORD=$password
psql -U $user -p $port -h $host -d $dbname -f $main_path/dbscripts/postgresql/onap-holmes_engine-createobj.sql
psql -U $user -p $port -h $host -d $dbname --command 'select * from alarm_info;'
sql_result=$?
unset PGPASSWORD
echo "sql_result=$sql_result"
if [ $sql_result != 0 ] ; then
   echo "Failed to initialize the database!"
   exit 1
fi
echo "The database is initialized successfully!"
exit 0

