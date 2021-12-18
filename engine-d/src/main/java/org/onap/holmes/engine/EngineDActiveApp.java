/**
 * Copyright 2017-2022 ZTE Corporation.
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

import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.ConfigFileScanner;
import org.onap.holmes.engine.dcae.ConfigFileScanningTask;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@ComponentScan(basePackages = {"org.onap.holmes"})
public class EngineDActiveApp implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(EngineDActiveApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(
                new ConfigFileScanningTask(new ConfigFileScanner()), 60L,
                ConfigFileScanningTask.POLLING_PERIOD, TimeUnit.SECONDS);

        Initializer.setReadyForMsbReg(true);
    }
}
