/*
 * Copyright 2017-2021 ZTE Corporation.
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.common.utils.SpringContextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Slf4j
public class Subscriber {

    private DMaaPResponseUtil dMaaPResponseUtil = SpringContextUtil.getBean(DMaaPResponseUtil.class);

    /**
     * The number of milliseconds to wait for messages if none are immediately available. This
     * should normally be used, and set at 15000 or higher.
     */
    private int timeout = 15000;

    /**
     * The maximum number of messages to return
     */
    private int limit = 100;

    /**
     * The number of milliseconds to poll interval time. This should normally be used, and set at
     * 15000 or higher.
     */
    private int period = timeout;

    private boolean secure;
    private String topic;
    private String url;
    private String uuid = UUID.randomUUID() + "";
    private String consumerGroup = "homlesGroup" + uuid;
    private String consumer = "homles" + uuid;
    private String authInfo;
    private String authExpDate;

    public List<VesAlarm> subscribe() throws CorrelationException {
        List<String> response;
        try {
            response = getDMaaPData();
        } catch (Exception e) {
            throw new CorrelationException("Failed to get data from DMaaP.", e);
        }
        try {
            return extractVesAlarm(response);
        } catch (Exception e) {
            throw new CorrelationException("Failed to convert the response data to VES alarms.", e);
        }
    }

    private List<String> getDMaaPData() {
        return JerseyClient.newInstance()
                .path(consumerGroup)
                .path(consumer)
                .queryParam("timeout", period)
                .get(url, List.class);
    }

    private List<VesAlarm> extractVesAlarm(List<String> responseEntity) {
        List<VesAlarm> vesAlarmList = new ArrayList<>();
        for (String entity : responseEntity) {
            try {
       		vesAlarmList.add(dMaaPResponseUtil.convertJsonToVesAlarm(entity));
       	    } catch (Exception e) {
        	log.error("Failed to convert the response data to VES alarm ", e);
        	//Continue with other events
            }
	}
        return vesAlarmList;
    }
}
