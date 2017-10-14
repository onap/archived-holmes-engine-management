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
package org.onap.holmes.engine.dcaepolling;

import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.dcae.DcaeConfigurationQuery;
import org.onap.holmes.common.dcae.DcaeConfigurationsCache;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.dmaappolling.SubscriberAction;

@Slf4j
public class DcaeConfigurationPolling implements Runnable{

    private String hostname;

    private String subscriberKey = "sec_fault_unsecure";

    public static long POLLING_PERIOD = 10 * 1000L;

    public DcaeConfigurationPolling(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void run() {
        DcaeConfigurations dcaeConfigurations = null;
        try {
            dcaeConfigurations = DcaeConfigurationQuery
                    .getDcaeConfigurations(hostname);
        } catch (CorrelationException e) {
            log.error("Failed to polling dcae configurations" + e.getMessage());
        }
        if (dcaeConfigurations != null) {
            DcaeConfigurationsCache.setDcaeConfigurations(dcaeConfigurations);
            addSubscriber(dcaeConfigurations);
        }
    }

    private void addSubscriber(DcaeConfigurations dcaeConfigurations) {
        SubscriberAction subscriberAction = ServiceLocatorHolder.getLocator()
                .getService(SubscriberAction.class);
        Subscriber subscriber = new Subscriber();
        subscriber.setUrl(dcaeConfigurations.getSubSecInfo(subscriberKey).getDmaapInfo()
                .getTopicUrl());
        subscriberAction.addSubscriber(subscriber);
    }
}
