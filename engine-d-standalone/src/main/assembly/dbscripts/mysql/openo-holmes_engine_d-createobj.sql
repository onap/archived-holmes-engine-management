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
/******************drop old database and user***************************/
use mysql;
drop database IF  EXISTS holmes;
delete from user where User='holmes';
FLUSH PRIVILEGES;

/******************create new database and user***************************/
create database holmes CHARACTER SET utf8;

GRANT ALL PRIVILEGES ON holmes.* TO 'holmes'@'%' IDENTIFIED BY 'holmes' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'holmes'@'%' IDENTIFIED BY 'holmes' WITH GRANT OPTION;

GRANT ALL PRIVILEGES ON catalog.* TO 'holmes'@'localhost' IDENTIFIED BY 'holmes' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'holmes'@'localhost' IDENTIFIED BY 'holmes' WITH GRANT OPTION;
FLUSH PRIVILEGES;

use holmes;
set Names 'utf8';
/******************delete old table and create new***************************/
use holmes;
DROP TABLE IF EXISTS aplus_rule;
CREATE TABLE aplus_rule (
  rid varchar(30) NOT NULL,
  name varchar(150) CHARACTER NOT NULL,
  description varchar(4000) CHARACTER SET  DEFAULT NULL,
  enable int(1) NOT NULL,
  templateID int(10) NOT NULL,
  engineID varchar(20) CHARACTER  NOT NULL,
  engineType varchar(20) CHARACTER  NOT NULL,
  creator varchar(20) CHARACTER  NOT NULL,
  createTime datetime NOT NULL,
  updator varchar(20) CHARACTER  DEFAULT NULL,
  updateTime datetime DEFAULT NULL,
  params varchar(4000) CHARACTER  DEFAULT NULL,
  content varchar(4000) CHARACTER  NOT NULL,
  vendor varchar(100) CHARACTER  NOT NULL,
  package varchar(255) DEFAULT NULL,
  PRIMARY KEY (rid),
  UNIQUE KEY name (name),
  KEY IDX_APLUS_RULE_ENABLE (enable),
  KEY IDX_APLUS_RULE_TEMPLATEID (templateID),
  KEY IDX_APLUS_RULE_ENGINEID (engineID),
  KEY IDX_APLUS_RULE_ENGINETYPE (engineType)
)

