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
package org.onap.holmes.engine.dmaap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.api.entity.AlarmInfo;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.db.AlarmInfoDaoService;
import org.onap.holmes.engine.manager.DroolsEngine;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@PrepareForTest({Subscriber.class, DroolsEngine.class, DMaaPAlarmPolling.class})
@RunWith(PowerMockRunner.class)
public class DMaaPAlarmPollingTest {

    private DMaaPAlarmPolling dMaaPAlarmPolling;
    private Subscriber subscriber;
    private DroolsEngine droolsEngine;
    private AlarmInfoDaoService alarmInfoDaoService;

    @Before
    public void setUp() {
        subscriber = PowerMock.createMock(Subscriber.class);
        droolsEngine = PowerMock.createMock(DroolsEngine.class);
        alarmInfoDaoService = PowerMock.createMock(AlarmInfoDaoService.class);
        dMaaPAlarmPolling = new DMaaPAlarmPolling(subscriber, droolsEngine, alarmInfoDaoService);
        PowerMock.replayAll();
    }

    @Test
    public void test_stop_task_ok() throws Exception {
        dMaaPAlarmPolling.stopTask();
        Field field = DMaaPAlarmPolling.class.getDeclaredField("isAlive");
        field.setAccessible(true);
        assertThat(field.get(dMaaPAlarmPolling), equalTo(false));
    }

    @Test
    public void testGetAlarmInfo() throws Exception {
        VesAlarm vesAlarm = new VesAlarm();
        vesAlarm.setAlarmIsCleared(1);
        vesAlarm.setSourceName("sourceName");
        vesAlarm.setSourceId("sourceId");
        vesAlarm.setStartEpochMicrosec(1L);
        vesAlarm.setLastEpochMicrosec(1L);
        vesAlarm.setEventName("eventName");
        vesAlarm.setEventId("eventId");
        vesAlarm.setRootFlag(0);

        PowerMock.replayAll();
        AlarmInfo alarmInfo = Whitebox.invokeMethod(dMaaPAlarmPolling, "getAlarmInfo", vesAlarm);
        PowerMock.verifyAll();

        assertThat(alarmInfo.getAlarmIsCleared(), is(1));
        assertThat(alarmInfo.getSourceName(), equalTo("sourceName"));
        assertThat(alarmInfo.getSourceId(), equalTo("sourceId"));
        assertThat(alarmInfo.getStartEpochMicroSec(), is(1L));
        assertThat(alarmInfo.getLastEpochMicroSec(), is(1L));
        assertThat(alarmInfo.getEventName(), equalTo("eventName"));
        assertThat(alarmInfo.getEventId(), equalTo("eventId"));
        assertThat(alarmInfo.getRootFlag(), is(0));
    }

}
