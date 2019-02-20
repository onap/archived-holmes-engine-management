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
package org.onap.holmes.dsa.dmaappolling;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.onap.holmes.common.api.stat.AlarmAdditionalField;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.utils.GsonUtil;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.dsa.dmaappolling.DMaaPResponseUtil;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@PrepareForTest({ServiceLocatorHolder.class, ServiceLocator.class, HttpsUtils.class})
@RunWith(PowerMockRunner.class)
public class SubscriberTest {

    private DMaaPResponseUtil util = new DMaaPResponseUtil();

    @Before
    public void init() {
        PowerMockito.mockStatic(ServiceLocatorHolder.class);
        ServiceLocator serviceLocator = PowerMockito.mock(ServiceLocator.class);
        PowerMockito.when(ServiceLocatorHolder.getLocator()).thenReturn(serviceLocator);
        PowerMockito.when(serviceLocator.getService(DMaaPResponseUtil.class)).thenReturn(util);
    }

    @Test
    public void subscribe() throws Exception {
        PowerMock.resetAll();
        VesAlarm vesAlarm = new VesAlarm();
        vesAlarm.setDomain("ONAP");
        vesAlarm.setEventId("123");
        vesAlarm.setEventName("Event-123");
        vesAlarm.setEventType("EventType");
        vesAlarm.setLastEpochMicrosec(1000L);
        vesAlarm.setNfcNamingCode("123");
        vesAlarm.setNfNamingCode("123");
        vesAlarm.setPriority("high");
        vesAlarm.setReportingEntityId("ID-123");
        vesAlarm.setReportingEntityName("Name-123");
        vesAlarm.setSequence(1);
        vesAlarm.setSourceId("Source-123");
        vesAlarm.setSourceName("Source-123");
        vesAlarm.setStartEpochMicrosec(500L);
        vesAlarm.setVersion(1L);
        List<AlarmAdditionalField> alarmAdditionalFields = new ArrayList<>();
        AlarmAdditionalField field = new AlarmAdditionalField();
        field.setName("addInfo");
        field.setValue("addInfo");
        alarmAdditionalFields.add(field);
        vesAlarm.setAlarmAdditionalInformation(alarmAdditionalFields);
        vesAlarm.setAlarmCondition("alarmCondition");
        vesAlarm.setAlarmInterfaceA("alarmInterfaceA");
        vesAlarm.setEventCategory("eventCategory");
        vesAlarm.setEventSeverity("eventSeverity");
        vesAlarm.setEventSourceType("eventSourceType");
        vesAlarm.setFaultFieldsVersion(1L);
        vesAlarm.setSpecificProblem("specificProblem");
        vesAlarm.setVfStatus("vfStatus");

        String eventString = "{\"event\": {\"commonEventHeader\": {" +
                "\"domain\": \"ONAP\"," +
                "\"eventId\": \"123\"," +
                "\"eventName\": \"Event-123\"," +
                "\"eventType\": \"EventType\"," +
                "\"lastEpochMicrosec\": 1000," +
                "\"nfcNamingCode\": \"123\"," +
                "\"nfNamingCode\": \"123\"," +
                "\"priority\": \"high\"," +
                "\"reportingEntityId\": \"ID-123\"," +
                "\"reportingEntityName\": \"Name-123\"," +
                "\"sequence\": 1," +
                "\"sourceId\": \"Source-123\"," +
                "\"sourceName\": \"Source-123\"," +
                "\"startEpochMicrosec\": 500," +
                "\"version\": 1" +
                "}," +
                " \"faultFields\" : {" +
                "\"alarmAdditionalInformation\": [{\"name\":\"addInfo\", \"value\":\"addInfo\"}]," +
                "\"alarmCondition\": \"alarmCondition\"," +
                "\"alarmInterfaceA\": \"alarmInterfaceA\"," +
                "\"eventCategory\": \"eventCategory\"," +
                "\"eventSeverity\": \"eventSeverity\"," +
                "\"eventSourceType\": \"eventSourceType\"," +
                "\"faultFieldsVersion\": 1," +
                "\"specificProblem\": \"specificProblem\"," +
                "\"vfStatus\": \"vfStatus\"" +
                "}}}";
        Subscriber subscriber = new Subscriber();
        subscriber.setUrl("https://www.onap.org");
        subscriber.setConsumerGroup("group");
        subscriber.setConsumer("consumer");
        List<String> responseList = new ArrayList<>();
        responseList.add(eventString);
        String responseJson = GsonUtil.beanToJson(responseList);

        PowerMockito.mockStatic(HttpsUtils.class);
        HttpResponse httpResponse = PowerMockito.mock(HttpResponse.class);
        PowerMockito.when(HttpsUtils.get(Matchers.any(HttpGet.class),
                Matchers.any(HashMap.class), Matchers.any(CloseableHttpClient.class))).thenReturn(httpResponse);
        PowerMockito.when(HttpsUtils.extractResponseEntity(httpResponse)).thenReturn(responseJson);

        PowerMock.replayAll();

        List<VesAlarm> vesAlarms = subscriber.subscribe();
        PowerMock.verifyAll();

        assertThat(vesAlarm.getEventName(), equalTo(vesAlarms.get(0).getEventName()));
    }

    @Test
    public void testSetterAndGetter() {

        PowerMock.replayAll();

        Subscriber subscriber = new Subscriber();
        subscriber.setTimeout(100);
        subscriber.setLimit(10);
        subscriber.setPeriod(10);
        subscriber.setSecure(false);
        subscriber.setTopic("test");
        subscriber.setUrl("http://localhost");
        subscriber.setConsumerGroup("Group1");
        subscriber.setConsumer("Consumer1");
        subscriber.setAuthInfo(null);
        subscriber.setAuthExpDate(null);

        assertThat(subscriber.getTimeout(), is(100));
        assertThat(subscriber.getLimit(), is(10));
        assertThat(subscriber.getPeriod(), is(10));
        assertThat(subscriber.isSecure(), is(false));
        assertThat(subscriber.getTopic(), equalTo("test"));
        assertThat(subscriber.getUrl(), equalTo("http://localhost"));
        assertThat(subscriber.getConsumerGroup(), equalTo("Group1"));
        assertThat(subscriber.getConsumer(), equalTo("Consumer1"));
        assertThat(subscriber.getAuthInfo(), nullValue());
        assertThat(subscriber.getAuthExpDate(), nullValue());

        PowerMock.verifyAll();
    }

}