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

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.manager.DroolsEngine;

@Slf4j
public class DMaaPAlarmPolling implements Runnable {

    private Subscriber subscriber;
    private DroolsEngine droolsEngine;
    private volatile boolean isAlive = true;

    public DMaaPAlarmPolling(Subscriber subscriber, DroolsEngine droolsEngine) {
        this.subscriber = subscriber;
        this.droolsEngine = droolsEngine;
    }

    public void run() {
        while (isAlive) {
            List<VesAlarm> vesAlarmList = new ArrayList<>();
            try {
                vesAlarmList = subscriber.subscribe();
            } catch (CorrelationException e) {
                log.error("Failed polling request alarm." + e.getMessage());
            }
            vesAlarmList.forEach(vesAlarm -> droolsEngine.putRaisedIntoStream(vesAlarm));
        }
    }

    public void stopTask() {
        isAlive = false;
    }
}
