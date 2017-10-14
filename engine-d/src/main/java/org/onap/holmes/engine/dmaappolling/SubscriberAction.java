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
package org.onap.holmes.engine.dmaappolling;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.manager.DroolsEngine;

@Service
public class SubscriberAction {

    @Inject
    private DroolsEngine droolsEngine;

    private ConcurrentHashMap<String, ScheduledFuture> pollingRequests = new ConcurrentHashMap<String, ScheduledFuture>();
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public void addSubscriber(Subscriber subscriber) {
        if (!pollingRequests.containsKey(subscriber.getTopic())) {
            DMaaPPollingRequest pollingTask = new DMaaPPollingRequest(subscriber, droolsEngine);
            ScheduledFuture future = service
                    .scheduleAtFixedRate(pollingTask, 0, subscriber.getPeriod(), TimeUnit.MILLISECONDS);
            pollingRequests.put(subscriber.getTopic(), future);
        }
    }

    public void removeSubscriber(Subscriber subscriber) {
        ScheduledFuture future = pollingRequests.get(subscriber.getTopic());
        if (future != null) {
            future.cancel(true);
        }
        pollingRequests.remove(subscriber.getTopic());
    }
}
