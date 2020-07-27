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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.api.stat.AlarmAdditionalField;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


@PrepareForTest(DMaaPResponseUtil.class)
public class DMaaPResponseUtilTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DMaaPResponseUtil dMaaPResponseUtil;

    @Before
    public void setUp() {
        dMaaPResponseUtil = new DMaaPResponseUtil();
    }

    @Test
    public void testDMaaPResponseUtil_input_normal() throws IOException {
        String json = "{\"event\":{"
                + "\"commonEventHeader\":{"
                + "\"domain\":\"fault\","
                + "\"eventId\":\"112355255-24345\","
                + "\"eventName\":\"4333454333\","
                + "\"eventType\":\"remote\","
                + "\"lastEpochMicrosec\":333434333,"
                + "\"nfcNamingCode\":\"567422\","
                + "\"nfNamingCode\":\"qweertrr\","
                + "\"priority\":\"High\","
                + "\"reportingEntityId\":\"99888888888\","
                + "\"reportingEntityName\":\"tianshisdlsdjf\","
                + "\"sequence\":34566,"
                + "\"sourceId\":\"3345556444\","
                + "\"sourceName\":\"nihoahh\","
                + "\"startEpochMicrosec\":54545454,"
                + "\"version\":998989879"
                + "},"
                + "\"faultFields\":{"
                + "\"alarmAdditionalInformation\":["
                + "{"
                + "\"name\":\"niaho\","
                + "\"value\":\"1111\""
                + "},"
                + "{"
                + "\"name\":\"tian\","
                + "\"value\":\"22222\""
                + "}"
                + "],"
                + "\"alarmCondition\":\"fadilel\","
                + "\"alarmInterfaceA\":\"generating the alarm\","
                + "\"eventCategory\":\"Event category\","
                + "\"eventSeverity\":\"CRITICAL\","
                + "\"eventSourceType\":\"type of event source\","
                + "\"faultFieldsVersion\":567567567,"
                + "\"specificProblem\":\"short description\","
                + "\"vfStatus\":\"Preparing to terminate\""
                + "}"
                + "}"
                + "}";
        VesAlarm expected = new VesAlarm();
        expected.setDomain("fault");
        expected.setEventId("112355255-24345");
        expected.setEventName("4333454333");
        expected.setEventType("remote");
        expected.setLastEpochMicrosec((long) 333434333);
        expected.setNfcNamingCode("567422");
        expected.setNfNamingCode("qweertrr");
        expected.setPriority("High");
        expected.setReportingEntityId("99888888888");
        expected.setReportingEntityName("tianshisdlsdjf");
        expected.setSequence(34566);
        expected.setSourceId("3345556444");
        expected.setSourceName("nihoahh");
        expected.setStartEpochMicrosec((long) 54545454);
        expected.setVersion((long) 998989879);

        List<AlarmAdditionalField> alarmAdditionalFields = new ArrayList<>();
        AlarmAdditionalField alarmAdditionalField1 = new AlarmAdditionalField();
        alarmAdditionalField1.setName("niaho");
        alarmAdditionalField1.setName("1111");
        alarmAdditionalFields.add(alarmAdditionalField1);
        AlarmAdditionalField alarmAdditionalField2 = new AlarmAdditionalField();
        alarmAdditionalField2.setName("tian");
        alarmAdditionalField2.setName("22222");
        alarmAdditionalFields.add(alarmAdditionalField2);
        expected.setAlarmAdditionalInformation(alarmAdditionalFields);

        expected.setAlarmCondition("fadilel");
        expected.setAlarmInterfaceA("generating the alarm");
        expected.setEventCategory("Event category");
        expected.setEventSeverity("CRITICAL");
        expected.setEventSourceType("type of event source");
        expected.setFaultFieldsVersion((long) 567567567);
        expected.setSpecificProblem("short description");
        expected.setVfStatus("Preparing to terminate");

        VesAlarm result = dMaaPResponseUtil.convertJsonToVesAlarm(json);

        assertThat(expected.getDomain(), equalTo(result.getDomain()));
        assertThat(expected.getEventId(), equalTo(result.getEventId()));
        assertThat(expected.getEventName(), equalTo(result.getEventName()));
        assertThat(expected.getEventType(), equalTo(result.getEventType()));
        assertThat(expected.getLastEpochMicrosec(), equalTo(result.getLastEpochMicrosec()));
        assertThat(expected.getNfcNamingCode(), equalTo(result.getNfcNamingCode()));
        assertThat(expected.getNfNamingCode(), equalTo(result.getNfNamingCode()));
        assertThat(expected.getPriority(), equalTo(result.getPriority()));
        assertThat(expected.getReportingEntityId(), equalTo(result.getReportingEntityId()));
        assertThat(expected.getReportingEntityName(), equalTo(result.getReportingEntityName()));
        assertThat(expected.getSequence(), equalTo(result.getSequence()));
        assertThat(expected.getSourceId(), equalTo(result.getSourceId()));
        assertThat(expected.getSourceName(), equalTo(result.getSourceName()));
        assertThat(expected.getStartEpochMicrosec(), equalTo(result.getStartEpochMicrosec()));
        assertThat(expected.getVersion(), equalTo(result.getVersion()));

        assertThat(expected.getAlarmCondition(), equalTo(result.getAlarmCondition()));
        assertThat(expected.getAlarmInterfaceA(), equalTo(result.getAlarmInterfaceA()));
        assertThat(expected.getEventCategory(), equalTo(result.getEventCategory()));
        assertThat(expected.getEventSeverity(), equalTo(result.getEventSeverity()));
        assertThat(expected.getEventSourceType(), equalTo(result.getEventSourceType()));
        assertThat(expected.getFaultFieldsVersion(), equalTo(result.getFaultFieldsVersion()));
        assertThat(expected.getSpecificProblem(), equalTo(result.getSpecificProblem()));
        assertThat(expected.getVfStatus(), equalTo(result.getVfStatus()));

    }

    @Test
    public void testDMaaPResponseUtil_throws_nullPointerException() throws Exception {
        String json = "{}";
        thrown.expect(NullPointerException.class);
        dMaaPResponseUtil.convertJsonToVesAlarm(json);
    }

    @Test
    public void testDMaaPResponseUtil_input_illegal() throws Exception {
        String json = "***";
        thrown.expect(Exception.class);
        dMaaPResponseUtil.convertJsonToVesAlarm(json);
    }

    @Test
    public void testDMaaPResponseUtil_only_necessary_information() throws IOException {
        String json = "{\"event\":{"
                + "\"commonEventHeader\":{"
                + "\"domain\":\"fault\","
                + "\"eventId\":\"112355255-24345\","
                + "\"eventName\":\"4333454333\","
                + "\"eventType\":\"remote\","
                + "\"lastEpochMicrosec\":333434333,"
                + "\"priority\":\"High\","
                + "\"reportingEntityName\":\"tianshisdlsdjf\","
                + "\"sequence\":34566,"
                + "\"sourceName\":\"nihoahh\","
                + "\"startEpochMicrosec\":54545454,"
                + "\"version\":998989879"
                + "},"
                + "\"faultFields\":{"
                + "\"alarmAdditionalInformation\":["
                + "{"
                + "\"name\":\"niaho\","
                + "\"value\":\"1111\""
                + "},"
                + "{"
                + "\"name\":\"tian\","
                + "\"value\":\"22222\""
                + "}"
                + "],"
                + "\"alarmCondition\":\"fadilel\","
                + "\"eventSeverity\":\"CRITICAL\","
                + "\"eventSourceType\":\"type of event source\","
                + "\"faultFieldsVersion\":567567567,"
                + "\"specificProblem\":\"short description\","
                + "\"vfStatus\":\"Preparing to terminate\""
                + "}"
                + "}"
                + "}";
        VesAlarm expected = new VesAlarm();
        expected.setDomain("fault");
        expected.setEventId("112355255-24345");
        expected.setEventName("4333454333");
        expected.setPriority("High");
        expected.setReportingEntityName("tianshisdlsdjf");
        expected.setSequence(34566);
        expected.setSourceName("nihoahh");
        expected.setStartEpochMicrosec((long) 54545454);
        expected.setVersion((long) 998989879);

        List<AlarmAdditionalField> alarmAdditionalFields = new ArrayList<>();
        AlarmAdditionalField alarmAdditionalField1 = new AlarmAdditionalField();
        alarmAdditionalField1.setName("niaho");
        alarmAdditionalField1.setName("1111");
        alarmAdditionalFields.add(alarmAdditionalField1);
        AlarmAdditionalField alarmAdditionalField2 = new AlarmAdditionalField();
        alarmAdditionalField2.setName("tian");
        alarmAdditionalField2.setName("22222");
        alarmAdditionalFields.add(alarmAdditionalField2);
        expected.setAlarmAdditionalInformation(alarmAdditionalFields);

        expected.setAlarmCondition("fadilel");
        expected.setEventSeverity("CRITICAL");
        expected.setEventSourceType("type of event source");
        expected.setFaultFieldsVersion((long) 567567567);
        expected.setSpecificProblem("short description");
        expected.setVfStatus("Preparing to terminate");

        VesAlarm result = dMaaPResponseUtil.convertJsonToVesAlarm(json);

        assertThat(expected.getDomain(), equalTo(result.getDomain()));
        assertThat(expected.getEventId(), equalTo(result.getEventId()));
        assertThat(expected.getEventName(), equalTo(result.getEventName()));
        assertThat(expected.getPriority(), equalTo(result.getPriority()));
        assertThat(expected.getReportingEntityName(), equalTo(result.getReportingEntityName()));
        assertThat(expected.getSequence(), equalTo(result.getSequence()));
        assertThat(expected.getSourceName(), equalTo(result.getSourceName()));
        assertThat(expected.getStartEpochMicrosec(), equalTo(result.getStartEpochMicrosec()));
        assertThat(expected.getVersion(), equalTo(result.getVersion()));

        assertThat(expected.getAlarmCondition(), equalTo(result.getAlarmCondition()));
        assertThat(expected.getEventSeverity(), equalTo(result.getEventSeverity()));
        assertThat(expected.getEventSourceType(), equalTo(result.getEventSourceType()));
        assertThat(expected.getFaultFieldsVersion(), equalTo(result.getFaultFieldsVersion()));
        assertThat(expected.getSpecificProblem(), equalTo(result.getSpecificProblem()));
        assertThat(expected.getVfStatus(), equalTo(result.getVfStatus()));
    }

    @Test
    public void testDMaaPResponseUtil_input_array_illegal() throws IOException {
        String json = "{\"event\":{"
                + "\"commonEventHeader\":{"
                + "\"domain\":\"fault\","
                + "\"eventId\":\"112355255-24345\","
                + "\"eventName\":\"4333454333\","
                + "\"eventType\":\"remote\","
                + "\"lastEpochMicrosec\":333434333,"
                + "\"priority\":\"High\","
                + "\"reportingEntityName\":\"tianshisdlsdjf\","
                + "\"sequence\":34566,"
                + "\"sourceName\":\"nihoahh\","
                + "\"startEpochMicrosec\":54545454,"
                + "\"version\":998989879"
                + "},"
                + "\"faultFields\":{"
                + "\"alarmAdditionalInformation\":["
                + "{"
                + "\"nam\":\"niaho\","
                + "\"value\":\"1111\""
                + "},"
                + "{"
                + "\"name\":\"tian\","
                + "\"value\":\"22222\""
                + "}"
                + "],"
                + "\"alarmCondition\":\"fadilel\","
                + "\"eventSeverity\":\"CRITICAL\","
                + "\"eventSourceType\":\"type of event source\","
                + "\"faultFieldsVersion\":567567567,"
                + "\"specificProblem\":\"short description\","
                + "\"vfStatus\":\"Preparing to terminate\""
                + "}"
                + "}"
                + "}";
        VesAlarm expected = new VesAlarm();
        expected.setDomain("fault");
        expected.setEventId("112355255-24345");
        expected.setEventName("4333454333");
        expected.setPriority("High");
        expected.setReportingEntityName("tianshisdlsdjf");
        expected.setSequence(34566);
        expected.setSourceName("nihoahh");
        expected.setStartEpochMicrosec((long) 54545454);
        expected.setVersion((long) 998989879);

        List<AlarmAdditionalField> alarmAdditionalFields = new ArrayList<>();
        AlarmAdditionalField alarmAdditionalField = new AlarmAdditionalField();
        alarmAdditionalField.setName(null);
        alarmAdditionalField.setValue("1111");
        alarmAdditionalFields.add(alarmAdditionalField);
        alarmAdditionalField = new AlarmAdditionalField();
        alarmAdditionalField.setName("tian");
        alarmAdditionalField.setValue("22222");
        alarmAdditionalFields.add(alarmAdditionalField);
        expected.setAlarmAdditionalInformation(alarmAdditionalFields);

        expected.setAlarmCondition("fadilel");
        expected.setEventSeverity("CRITICAL");
        expected.setEventSourceType("type of event source");
        expected.setFaultFieldsVersion((long) 567567567);
        expected.setSpecificProblem("short description");
        expected.setVfStatus("Preparing to terminate");

        VesAlarm result = dMaaPResponseUtil.convertJsonToVesAlarm(json);

        assertThat(expected.getDomain(), equalTo(result.getDomain()));
        assertThat(expected.getEventId(), equalTo(result.getEventId()));
        assertThat(expected.getEventName(), equalTo(result.getEventName()));
        assertThat(expected.getPriority(), equalTo(result.getPriority()));
        assertThat(expected.getReportingEntityName(), equalTo(result.getReportingEntityName()));
        assertThat(expected.getSequence(), equalTo(result.getSequence()));
        assertThat(expected.getSourceName(), equalTo(result.getSourceName()));
        assertThat(expected.getStartEpochMicrosec(), equalTo(result.getStartEpochMicrosec()));
        assertThat(expected.getVersion(), equalTo(result.getVersion()));

        assertThat(expected.getAlarmAdditionalInformation().get(0).getName(),
                equalTo(result.getAlarmAdditionalInformation().get(0).getName()));
        assertThat(expected.getAlarmAdditionalInformation().get(0).getValue(),
                equalTo(result.getAlarmAdditionalInformation().get(0).getValue()));

        assertThat(expected.getAlarmCondition(), equalTo(result.getAlarmCondition()));
        assertThat(expected.getEventSeverity(), equalTo(result.getEventSeverity()));
        assertThat(expected.getEventSourceType(), equalTo(result.getEventSourceType()));
        assertThat(expected.getFaultFieldsVersion(), equalTo(result.getFaultFieldsVersion()));
        assertThat(expected.getSpecificProblem(), equalTo(result.getSpecificProblem()));
        assertThat(expected.getVfStatus(), equalTo(result.getVfStatus()));
    }
}
