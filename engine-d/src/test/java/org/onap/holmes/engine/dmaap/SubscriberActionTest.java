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

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class SubscriberActionTest {

    private SubscriberAction subscriberAction;

    @Before
    public void setUp() {
        subscriberAction = new SubscriberAction();
        HashMap<String, DMaaPAlarmPolling> dMaaPAlarmPollingHashMap = new HashMap<>();
        DMaaPAlarmPolling dMaaPAlarmPolling = new DMaaPAlarmPolling(null, null,null);
        dMaaPAlarmPollingHashMap.put("test", dMaaPAlarmPolling);
        DMaaPAlarmPolling dMaaPAlarmPolling1 = new DMaaPAlarmPolling(null, null,null);
        dMaaPAlarmPollingHashMap.put("testTopic", dMaaPAlarmPolling1);
        Whitebox.setInternalState(subscriberAction, "pollingTasks", dMaaPAlarmPollingHashMap);
        PowerMock.replayAll();
    }

    @Test
    public void removeSubscriber() throws Exception {
        Subscriber subscriber = PowerMock.createMock(Subscriber.class);
        PowerMock.expectPrivate(subscriber, "getTopic").andReturn("testTopic");
        PowerMock.expectPrivate(subscriber, "getUrl").andReturn("https");
        PowerMock.replayAll();
        subscriberAction.removeSubscriber(subscriber);
        PowerMock.verifyAll();
    }

    @Test
    public void stopPollingTasks() throws Exception {
        subscriberAction.stopPollingTasks();
        PowerMock.verifyAll();
    }

}