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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.manager.DroolsEngine;

@Service
public class SubscriberAction {

    @Inject
    private DroolsEngine droolsEngine;
    private ConcurrentHashMap<String, DMaaPAlarmPolling> pollingTasks = new ConcurrentHashMap<>();

    public void addSubscriber(Subscriber subscriber) {
        if (!pollingTasks.containsKey(subscriber.getTopic())) {
            DMaaPAlarmPolling pollingTask = new DMaaPAlarmPolling(subscriber, droolsEngine);
            Thread thread = new Thread(pollingTask);
            thread.start();
            pollingTasks.put(subscriber.getTopic(), pollingTask);
        }
    }

    public void removeSubscriber(Subscriber subscriber) {
        if (pollingTasks.containsKey(subscriber.getTopic())) {
            pollingTasks.get(subscriber.getTopic()).stopTask();
            pollingTasks.remove(subscriber.getTopic());
        }
    }

    @PreDestroy
    public void stopPollingTasks() {
        Iterator iterator = pollingTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String) entry.getKey();
            pollingTasks.get(key).stopTask();
        }
        pollingTasks.clear();
    }
}
