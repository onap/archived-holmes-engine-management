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
package org.openo.holmes.engine;

import io.dropwizard.setup.Environment;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.openo.dropwizard.ioc.bundle.IOCApplication;
import org.openo.holmes.common.api.entity.ServiceRegisterEntity;
import org.openo.holmes.common.config.MicroServiceConfig;
import org.openo.holmes.common.utils.MSBRegisterUtil;

@Slf4j
public class EngineDActiveApp extends IOCApplication<EngineDAppConfig> {

    public static void main(String[] args) throws Exception {
        new EngineDActiveApp().run(args);
    }

    @Override
    public void run(EngineDAppConfig configuration, Environment environment) throws Exception {
        super.run(configuration, environment);

        try {
            new MSBRegisterUtil().register(initServiceEntity());
        } catch (IOException e) {
            log.warn("Micro service registry httpclient close failure", e);
        }
    }

    private ServiceRegisterEntity initServiceEntity() {
        ServiceRegisterEntity serviceRegisterEntity = new ServiceRegisterEntity();
        serviceRegisterEntity.setServiceName("holmes-engine-mgmt");
        serviceRegisterEntity.setProtocol("REST");
        serviceRegisterEntity.setVersion("v1");
        serviceRegisterEntity.setUrl("/openoapi/holmes-engine-mgmt/v1");
        serviceRegisterEntity.setSingleNode(MicroServiceConfig.getServiceIp(), "9102", 0);
        serviceRegisterEntity.setVisualRange("1|0");
        return serviceRegisterEntity;
    }
}
