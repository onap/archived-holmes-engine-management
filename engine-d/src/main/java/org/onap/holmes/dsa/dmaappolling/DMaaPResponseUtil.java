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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.stat.AlarmAdditionalField;
import org.onap.holmes.common.api.stat.VesAlarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DMaaPResponseUtil {

    public VesAlarm convertJsonToVesAlarm(String responseJson) throws IOException {
        JSONObject jsonNode = JSON.parseObject(responseJson);

        VesAlarm vesAlarm = new VesAlarm();

        JSONObject eventJson = JSON.parseObject(jsonNode.get("event") +"");
        JSONObject commonEventHeaderJson = JSON.parseObject(eventJson.get("commonEventHeader") +"");
        convertCommonEventHeaderJsonToEvent(commonEventHeaderJson, vesAlarm);

        JSONObject faultFieldsJson = JSON.parseObject(eventJson.get("faultFields") +"");
        convertFaultFieldsJsonToEvent(faultFieldsJson, vesAlarm);
        return vesAlarm;
    }

    private void convertCommonEventHeaderJsonToEvent(JSONObject commonEventHeaderJson,
                                                     VesAlarm vesAlarm) {
        vesAlarm.setDomain((String) commonEventHeaderJson.get("domain"));
        vesAlarm.setEventId((String) commonEventHeaderJson.get("eventId"));
        vesAlarm.setEventName((String) commonEventHeaderJson.get("eventName"));
        vesAlarm.setAlarmIsCleared(vesAlarm.getEventName().endsWith("Cleared") ? 1 : 0);
        vesAlarm.setEventType(getTextElementByNode(commonEventHeaderJson, "eventType"));
        vesAlarm.setInternalHeaderFields(
                getTextElementByNode(commonEventHeaderJson, "internalHeaderFields"));
        vesAlarm.setLastEpochMicrosec(commonEventHeaderJson.getLong("lastEpochMicrosec"));
        vesAlarm.setNfcNamingCode(getTextElementByNode(commonEventHeaderJson, "nfcNamingCode"));
        vesAlarm.setNfNamingCode(getTextElementByNode(commonEventHeaderJson, "nfNamingCode"));
        vesAlarm.setPriority((String) commonEventHeaderJson.get("priority"));
        vesAlarm.setReportingEntityId(
                getTextElementByNode(commonEventHeaderJson, "reportingEntityId"));
        vesAlarm.setReportingEntityName( (String) commonEventHeaderJson.get("reportingEntityName"));
        vesAlarm.setSequence((Integer) commonEventHeaderJson.get("sequence"));
        vesAlarm.setSourceId(getTextElementByNode(commonEventHeaderJson, "sourceId"));
        vesAlarm.setSourceName( (String) commonEventHeaderJson.get("sourceName"));
        vesAlarm.setStartEpochMicrosec(commonEventHeaderJson.getLong("startEpochMicrosec"));
        vesAlarm.setVersion(commonEventHeaderJson.getLong("version"));
    }

    private void convertFaultFieldsJsonToEvent(JSONObject faultFieldsJson, VesAlarm vesAlarm) {
        vesAlarm.setAlarmAdditionalInformation(getListElementByNode(faultFieldsJson, "alarmAdditionalInformation"));
        vesAlarm.setAlarmCondition(faultFieldsJson.getString("alarmCondition"));
        vesAlarm.setAlarmInterfaceA(getTextElementByNode(faultFieldsJson, "alarmInterfaceA"));
        vesAlarm.setEventCategory(getTextElementByNode(faultFieldsJson,"eventCategory"));
        vesAlarm.setEventSeverity(faultFieldsJson.getString("eventSeverity"));
        vesAlarm.setEventSourceType(faultFieldsJson.getString("eventSourceType"));
        vesAlarm.setFaultFieldsVersion(faultFieldsJson.getLong("faultFieldsVersion"));
        vesAlarm.setSpecificProblem(faultFieldsJson.getString("specificProblem"));
        vesAlarm.setVfStatus(faultFieldsJson.getString("vfStatus"));
    }

    private String getTextElementByNode(JSONObject jsonNode,String name){
        if(jsonNode.get(name) != null){
            return jsonNode.getString(name);
        }
        return null;
    }

    private Long getLongElementByNode(JSONObject jsonNode, String name) {
        if(jsonNode.get(name) != null){
            return jsonNode.getLong(name);
        }
        return null;
    }

    private List<AlarmAdditionalField> getListElementByNode(JSONObject jsonNode, String name){
        List<AlarmAdditionalField> alarms = new ArrayList<AlarmAdditionalField>();
        if (jsonNode.get(name) != null) {
            JSONArray alarmAdditionalInformations = jsonNode.getJSONArray(name);
            for (int i = 0; i < alarmAdditionalInformations.size(); i++) {
                JSONObject jsonObject = alarmAdditionalInformations.getJSONObject(i);
                if (jsonObject.get("name") != null
                        && jsonObject.get("value") != null) {
                    AlarmAdditionalField field = new AlarmAdditionalField();
                    field.setName(getTextElementByNode(jsonObject, "name"));
                    field.setValue(getTextElementByNode(jsonObject, "value"));
                    alarms.add(field);
                }
            }
        }
        return alarms;
    }
}
