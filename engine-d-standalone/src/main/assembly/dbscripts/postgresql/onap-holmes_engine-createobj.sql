--
-- Copyright 2017 ZTE Corporation.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
\c postgres

/******************CREATE NEW DATABASE AND USER***************************/
CREATE DATABASE DBNAME;

CREATE ROLE DBUSER with PASSWORD 'DBPWD' LOGIN;

\encoding UTF8;

/******************DELETE OLD TABLE AND CREATE NEW***************************/
\c DBNAME;

DROP TABLE IF EXISTS ALARM_INFO;

CREATE TABLE ALARM_INFO (
  EVENTID VARCHAR(150) NOT NULL,
  EVENTNAME VARCHAR(150) NOT NULL,
  ALARMISCLEARED SMALLINT NOT NULL,
  ROOTFLAG SMALLINT NOT NULL,
  STARTEPOCHMICROSEC BIGINT NOT NULL,
  LASTEPOCHMICROSEC BIGINT NOT NULL,
  SOURCEID VARCHAR(150)  NOT NULL,
  SOURCENAME VARCHAR(150)  NOT NULL,
  PRIMARY KEY (EVENTID),
);

GRANT ALL PRIVILEGES ON ALARM_INFO TO DBUSER;

