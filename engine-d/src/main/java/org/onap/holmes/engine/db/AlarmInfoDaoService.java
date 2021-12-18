/**
 * Copyright 2021-2022 ZTE Corporation.
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
package org.onap.holmes.engine.db;

import org.onap.holmes.common.api.entity.AlarmInfo;
import org.onap.holmes.common.exception.AlarmInfoException;
import org.onap.holmes.engine.db.jdbi.AlarmInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class AlarmInfoDaoService {

    @Autowired
    private AlarmInfoDao alarmInfoDao;

    public AlarmInfo saveAlarm(AlarmInfo alarmInfo) throws AlarmInfoException {
        try {
            alarmInfoDao.addAlarm(alarmInfo);
            return alarmInfo;
        } catch (Exception e) {
            throw new AlarmInfoException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public List<AlarmInfo> queryAllAlarm() throws AlarmInfoException {
        try {
            return alarmInfoDao.queryAlarm();
        } catch (Exception e) {
            throw new AlarmInfoException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public void deleteAlarm(AlarmInfo alarmInfo) {
        if (alarmInfo.getAlarmIsCleared() != 1) {
            return;
        }

        String sourceId = alarmInfo.getSourceId();
        String sourceName = alarmInfo.getSourceName();
        String eventName = alarmInfo.getEventName();
        eventName = eventName.substring(0, eventName.lastIndexOf("Cleared"));

        alarmInfoDao.deleteAlarmByAlarmIsCleared(eventName, sourceId, sourceName);
    }
}
