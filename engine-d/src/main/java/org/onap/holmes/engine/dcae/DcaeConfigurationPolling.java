/**
 * Copyright 2017 - 2021 ZTE Corporation.
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
package org.onap.holmes.engine.dcae;

import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.dcae.DcaeConfigurationQuery;
import org.onap.holmes.common.dcae.DcaeConfigurationsCache;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.Md5Util;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.dmaap.SubscriberAction;

@Slf4j
@Deprecated
public class DcaeConfigurationPolling implements Runnable {

    private String hostname;

    public static final long POLLING_PERIOD = 30 * 1000L;

    private String prevConfigMd5 = Md5Util.md5(null);

    public DcaeConfigurationPolling(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void run() {
        DcaeConfigurations dcaeConfigurations = null;
        try {
            dcaeConfigurations = DcaeConfigurationQuery.getDcaeConfigurations(hostname);
            String md5 = Md5Util.md5(dcaeConfigurations);
            if (prevConfigMd5.equals(md5)){
                log.info("Operation aborted due to identical Configurations.");
                return;
            }
            prevConfigMd5 = md5;
        } catch (CorrelationException e) {
            log.error("Failed to poll the DCAE configurations. " + e.getMessage(), e);
        } catch (Exception e) {
            log.info("Failed to generate the MD5 information for new configurations.", e);
        }
        if (dcaeConfigurations != null) {
            DcaeConfigurationsCache.setDcaeConfigurations(dcaeConfigurations);
            addSubscribers(dcaeConfigurations);
        }
    }

    private void addSubscribers(DcaeConfigurations dcaeConfigurations) {
        SubscriberAction subscriberAction = ServiceLocatorHolder.getLocator()
                .getService(SubscriberAction.class);
        for (String key : dcaeConfigurations.getSubKeys()) {
            Subscriber subscriber = new Subscriber();
            subscriber.setTopic(key);
            subscriber.setUrl(dcaeConfigurations.getSubSecInfo(key).getDmaapInfo()
                    .getTopicUrl());
            subscriberAction.addSubscriber(subscriber);
        }
    }
}
