/*
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.holmes.engine.dmaap;

import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.api.entity.AlarmInfo;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.exception.AlarmInfoException;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.db.AlarmInfoDao;
import org.onap.holmes.engine.manager.DroolsEngine;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DMaaPAlarmPolling implements Runnable {

    private Subscriber subscriber;
    private DroolsEngine droolsEngine;
    private volatile boolean isAlive = true;
    private AlarmInfoDao alarmInfoDao;


    public DMaaPAlarmPolling(Subscriber subscriber, DroolsEngine droolsEngine, AlarmInfoDao alarmInfoDao) {
        this.subscriber = subscriber;
        this.droolsEngine = droolsEngine;
        this.alarmInfoDao = alarmInfoDao;
    }

    public void run() {
        while (isAlive) {
            List<VesAlarm> vesAlarmList = new ArrayList<>();
            try {
                vesAlarmList = subscriber.subscribe();
                vesAlarmList.forEach(vesAlarm -> {
                    try {
                        alarmInfoDao.saveAlarm(getAlarmInfo(vesAlarm));
                        droolsEngine.putRaisedIntoStream(vesAlarm);
                    } catch(AlarmInfoException e) {
                        log.error("Failed to save alarm to database", e);
                    }
                });
            } catch (CorrelationException e) {
                log.error("Failed to process alarms. Sleep for 60 seconds to restart.", e);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    log.info("Thread is still active.", e);
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                log.error("An error occurred while processing alarm. Sleep for 60 seconds to restart.", e);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    log.info("Thread is still active.", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    private AlarmInfo getAlarmInfo(VesAlarm vesAlarm) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmIsCleared(vesAlarm.getAlarmIsCleared());
        alarmInfo.setSourceName(vesAlarm.getSourceName());
        alarmInfo.setSourceId(vesAlarm.getSourceId());
        alarmInfo.setStartEpochMicroSec(vesAlarm.getStartEpochMicrosec());
        alarmInfo.setLastEpochMicroSec(vesAlarm.getLastEpochMicrosec());
        alarmInfo.setEventId(vesAlarm.getEventId());
        alarmInfo.setEventName(vesAlarm.getEventName());
        alarmInfo.setRootFlag(vesAlarm.getRootFlag());
        return alarmInfo;
    }

    public void stopTask() {
        isAlive = false;
    }
}
