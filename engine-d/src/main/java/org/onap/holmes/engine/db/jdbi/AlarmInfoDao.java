/**
 * Copyright 2017-2021 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.holmes.engine.db.jdbi;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.onap.holmes.common.api.entity.AlarmInfo;
import org.onap.holmes.common.utils.AlarmInfoMapper;

import java.util.List;

@RegisterRowMapper(AlarmInfoMapper.class)
public interface AlarmInfoDao {

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO ALARM_INFO  (EVENTID,EVENTNAME,STARTEPOCHMICROSEC,SOURCEID,SOURCENAME,SEQUENCE,ALARMISCLEARED,ROOTFLAG,LASTEPOCHMICROSEC) VALUES (:eventId,:eventName,:startEpochMicroSec,:sourceId,:sourceName,:sequence,:alarmIsCleared,:rootFlag,:lastEpochMicroSec)")
    String addAlarm(@BindBean AlarmInfo alarmInfo);

    @SqlQuery("SELECT * FROM ALARM_INFO")
    List<AlarmInfo> queryAlarm();

    @SqlUpdate("DELETE FROM ALARM_INFO WHERE EVENTNAME=:eventName AND SOURCEID=:sourceId AND SOURCENAME=:sourceName")
    int deleteAlarmByAlarmIsCleared(@Bind("eventName") String eventName,
                                    @Bind("sourceId") String sourceId,
                                    @Bind("sourceName") String sourceName);
}
