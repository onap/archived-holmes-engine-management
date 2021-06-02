/**
 * Copyright 2017-2021 ZTE Corporation.
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
package org.onap.holmes.engine;

import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.dropwizard.ioc.bundle.IOCApplication;
import org.onap.holmes.common.utils.transactionid.TransactionIdFilter;
import org.onap.holmes.engine.dcae.DcaeConfigurationPolling;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EngineDActiveApp extends IOCApplication<EngineDAppConfig> {

    public static void main(String[] args) throws Exception {
        new EngineDActiveApp().run(args);
    }

    @Override
    public void run(EngineDAppConfig configuration, Environment environment) throws Exception {
        super.run(configuration, environment);

        if (!"1".equals(System.getenv("TESTING"))) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(
                    new DcaeConfigurationPolling(MicroServiceConfig.getEnv(MicroServiceConfig.HOSTNAME)), 0,
                    DcaeConfigurationPolling.POLLING_PERIOD, TimeUnit.MILLISECONDS);
        }

        environment.servlets().addFilter("logFilter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet
                .allOf(DispatcherType.class), true, "/*");
    }
}
