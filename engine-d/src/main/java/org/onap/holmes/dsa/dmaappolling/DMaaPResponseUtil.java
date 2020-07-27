/*
 * Copyright 2017-2020 ZTE Corporation.
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.stat.AlarmAdditionalField;
import org.onap.holmes.common.api.stat.VesAlarm;

import java.util.Collections;
import java.util.List;

import static org.onap.holmes.common.utils.GsonUtil.*;

@Service
public class DMaaPResponseUtil {

    public VesAlarm convertJsonToVesAlarm(String responseJson) {
        JsonObject jsonNode = JsonParser.parseString(responseJson).getAsJsonObject();

        VesAlarm vesAlarm = new VesAlarm();

        JsonObject eventJson = JsonParser.parseString(jsonNode.get("event").toString()).getAsJsonObject();
        JsonObject commonEventHeaderJson = JsonParser.parseString(eventJson.get("commonEventHeader").toString())
                .getAsJsonObject();
        convertCommonEventHeaderJsonToEvent(commonEventHeaderJson, vesAlarm);

        JsonObject faultFieldsJson = JsonParser.parseString(eventJson.get("faultFields").toString())
                .getAsJsonObject();
        convertFaultFieldsJsonToEvent(faultFieldsJson, vesAlarm);
        return vesAlarm;
    }

    private void convertCommonEventHeaderJsonToEvent(JsonObject commonEventHeaderJson,
                                                     VesAlarm vesAlarm) {
        vesAlarm.setDomain(getAsString(commonEventHeaderJson, "domain"));
        vesAlarm.setEventId(getAsString(commonEventHeaderJson, "eventId"));
        vesAlarm.setEventName(getAsString(commonEventHeaderJson, "eventName"));
        vesAlarm.setAlarmIsCleared(vesAlarm.getEventName().endsWith("Cleared") ? 1 : 0);
        vesAlarm.setEventType(getAsString(commonEventHeaderJson, "eventType"));
        vesAlarm.setInternalHeaderFields(getAsString(commonEventHeaderJson, "internalHeaderFields"));
        vesAlarm.setLastEpochMicrosec(getAsLong(commonEventHeaderJson, "lastEpochMicrosec"));
        vesAlarm.setNfcNamingCode(getAsString(commonEventHeaderJson, "nfcNamingCode"));
        vesAlarm.setNfNamingCode(getAsString(commonEventHeaderJson, "nfNamingCode"));
        vesAlarm.setPriority(getAsString(commonEventHeaderJson, "priority"));
        vesAlarm.setReportingEntityId(getAsString(commonEventHeaderJson, "reportingEntityId"));
        vesAlarm.setReportingEntityName(getAsString(commonEventHeaderJson, "reportingEntityName"));
        vesAlarm.setSequence(getAsInt(commonEventHeaderJson, "sequence"));
        vesAlarm.setSourceId(getAsString(commonEventHeaderJson, "sourceId"));
        vesAlarm.setSourceName(getAsString(commonEventHeaderJson, "sourceName"));
        vesAlarm.setStartEpochMicrosec(getAsLong(commonEventHeaderJson, "startEpochMicrosec"));
        vesAlarm.setVersion(getAsLong(commonEventHeaderJson, "version"));
    }

    private void convertFaultFieldsJsonToEvent(JsonObject faultFieldsJson, VesAlarm vesAlarm) {
        vesAlarm.setAlarmAdditionalInformation(getListElementByNode(faultFieldsJson, "alarmAdditionalInformation"));
        vesAlarm.setAlarmCondition(getAsString(faultFieldsJson, "alarmCondition"));
        vesAlarm.setAlarmInterfaceA(getAsString(faultFieldsJson, "alarmInterfaceA"));
        vesAlarm.setEventCategory(getAsString(faultFieldsJson, "eventCategory"));
        vesAlarm.setEventSeverity(getAsString(faultFieldsJson, "eventSeverity"));
        vesAlarm.setEventSourceType(getAsString(faultFieldsJson, "eventSourceType"));
        vesAlarm.setFaultFieldsVersion(getAsLong(faultFieldsJson, "faultFieldsVersion"));
        vesAlarm.setSpecificProblem(getAsString(faultFieldsJson, "specificProblem"));
        vesAlarm.setVfStatus(getAsString(faultFieldsJson, "vfStatus"));
    }

    private List<AlarmAdditionalField> getListElementByNode(JsonObject jsonNode, String name) {
        if (jsonNode.has(name) && !jsonNode.get(name).isJsonNull()) {
            return jsonToList(jsonNode.get(name).toString(), AlarmAdditionalField.class);
        }
        return Collections.emptyList();
    }
}
