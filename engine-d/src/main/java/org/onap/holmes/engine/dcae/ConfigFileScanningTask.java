/**
 * Copyright 2021 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.engine.dcae;

import org.onap.holmes.common.ConfigFileScanner;
import org.onap.holmes.common.dcae.DcaeConfigurationsCache;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.utils.DcaeConfigurationParser;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.Md5Util;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.dmaap.SubscriberAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConfigFileScanningTask implements Runnable {
    final public static long POLLING_PERIOD = 60L;
    final private static Logger LOGGER = LoggerFactory.getLogger(ConfigFileScanningTask.class);
    private String configFile = "/opt/hemtopics/cfy.json";
    private ConfigFileScanner configFileScanner;
    private String prevConfigMd5 = Md5Util.md5(null);

    public ConfigFileScanningTask(ConfigFileScanner configFileScanner) {
        this.configFileScanner = configFileScanner;
    }

    @Override
    public void run() {
        if (null == configFileScanner) {
            configFileScanner = new ConfigFileScanner();
        }
        Map<String, String> newConfig = configFileScanner.scan(configFile);
        String md5 = Md5Util.md5(newConfig);
        if (!prevConfigMd5.equals(md5)) {
            LOGGER.info("Configurations have changed.");
            prevConfigMd5 = md5;
        } else {
            return;
        }

        for (Map.Entry entry : newConfig.entrySet()) {
            DcaeConfigurations dcaeConfigurations = null;
            try {
                dcaeConfigurations = DcaeConfigurationParser.parse(entry.getValue().toString());
                if (dcaeConfigurations != null) {
                    DcaeConfigurationsCache.setDcaeConfigurations(dcaeConfigurations);
                    addSubscribers(dcaeConfigurations);
                }
            } catch (CorrelationException e) {
                LOGGER.error(e.getMessage(), e);
                // reset the value of the pre-md5 so that configs could be re-processed during the next scanning.
                prevConfigMd5 = null;
            } catch (Exception e) {
                LOGGER.warn("Failed to deal with the new configurations.", e);
                // reset the value of the pre-md5 so that configs could be re-processed during the next scanning.
                prevConfigMd5 = null;
            }
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
