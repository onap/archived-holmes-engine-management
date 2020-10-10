/**
 * Copyright 2020 ZTE Corporation.
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

package org.onap.holmes.engine.manager.status;

import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.engine.entity.EngineEntity;
import org.onap.holmes.common.engine.service.EngineEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class EntityStatusRefreshTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(EntityStatusRefreshTask.class);
    private final long INTERVAL = SECONDS.toMillis(15);
    private EngineEntity engineEntity;
    private Timer timer = new Timer("EntityStatusRefreshTimer", true);
    private EngineEntityService engineEntityService;

    @Inject
    public EntityStatusRefreshTask(EngineEntityService engineEntityService) {
        this.engineEntityService = engineEntityService;
    }

    @PostConstruct
    private void initialize() {
        String[] serviceAddrInfo = MicroServiceConfig.getMicroServiceIpAndPort();
        if (null != serviceAddrInfo) {
            String ip = serviceAddrInfo[0];
            String port = Optional.ofNullable(serviceAddrInfo[1]).orElse("9201");
            port = port.equals("80") ? "9201" : port;
            engineEntity = new EngineEntity(ip, Integer.parseInt(port));

            timer.schedule(this, SECONDS.toMillis(1), INTERVAL);
        } else {
            logger.warn("Failed to get the address info of current engine. " +
                    "Problems will be caused when it comes to a multi-instance scenario!");
        }
    }

    @Override
    public void run() {
        if (engineEntity == null) {
            logger.warn("No engine entity is specified. The status refresh task will return immediately.");
            return;
        }

        engineEntity.setLastModified(System.currentTimeMillis());

        try {
            if (engineEntityService.getEntity(engineEntity.getId()) == null) {
                engineEntityService.insertEntity(engineEntity);
            } else {
                engineEntityService.updateEntity(engineEntity);
            }
        } catch (Exception e) {
            logger.warn("Failed to update engine instance information.", e);
        }
    }
}
