/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.holmes.engine.dmaap;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.manager.DroolsEngine;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({Subscriber.class, DroolsEngine.class})
@RunWith(PowerMockRunner.class)
public class DMaaPAlarmPollingTest {

    private DMaaPAlarmPolling dMaaPAlarmPolling;
    private Subscriber subscriber;
    private DroolsEngine droolsEngine;

    @Before
    public void setUp() {
        subscriber = PowerMock.createMock(Subscriber.class);
        droolsEngine = PowerMock.createMock(DroolsEngine.class);
        dMaaPAlarmPolling = new DMaaPAlarmPolling(subscriber, droolsEngine);
        PowerMock.replayAll();
    }

    @Test
    public void test_stop_task_ok() throws Exception {
        dMaaPAlarmPolling.stopTask();
        Field field = DMaaPAlarmPolling.class.getDeclaredField("isAlive");
        field.setAccessible(true);
        assertThat(field.get(dMaaPAlarmPolling), equalTo(false));
    }

}