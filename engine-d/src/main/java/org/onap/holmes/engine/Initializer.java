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

import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.CommonUtils;
import org.onap.holmes.common.utils.MsbRegister;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.onap.msb.sdk.discovery.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.onap.holmes.common.config.MicroServiceConfig.getMicroServiceIpAndPort;
import static org.onap.holmes.common.utils.CommonUtils.getEnv;
import static org.onap.holmes.common.utils.CommonUtils.isIpAddress;

@Component
public class Initializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
    private volatile static boolean readyForMsbReg = false;
    private MsbRegister msbRegister;

    @Autowired
    public Initializer(MsbRegister msbRegister) {
        this.msbRegister = msbRegister;
    }

    @Override
    public void run(ApplicationArguments args) {
        Executors.newSingleThreadExecutor().execute(() -> {
            waitUntilReady();
            try {
                msbRegister.register2Msb(createMicroServiceInfo());
            } catch (CorrelationException e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    private void waitUntilReady() {
        int count = 1;
        while (!readyForMsbReg) {
            if (count > 20) {
                break;
            }
            int interval = 5 * count++;
            logger.info("Not ready for MSB registration. Try again after {} seconds...", interval);
            try {
                TimeUnit.SECONDS.sleep(interval);
            } catch (InterruptedException e) {
                logger.info(e.getMessage(), e);
            }
        }
    }

    public static void setReadyForMsbReg(boolean readyForMsbReg) {
        Initializer.readyForMsbReg = readyForMsbReg;
    }

    private MicroServiceInfo createMicroServiceInfo() {
        String[] serviceIpAndPort = getMicroServiceIpAndPort();
        MicroServiceInfo msinfo = new MicroServiceInfo();
        msinfo.setServiceName("holmes-engine-mgmt");
        msinfo.setVersion("v1");
        msinfo.setUrl("/api/holmes-engine-mgmt/v1");
        msinfo.setPath("/api/holmes-engine-mgmt/v1");
        msinfo.setProtocol("REST");
        msinfo.setVisualRange("0|1");
        msinfo.setLb_policy("round-robin");
        msinfo.setEnable_ssl(CommonUtils.isHttpsEnabled());
        Set<Node> nodes = new HashSet<>();
        Node node = new Node();
        node.setIp(isIpAddress(serviceIpAndPort[0]) ? serviceIpAndPort[0] : getEnv("HOLMES_ENGINE_MGMT_SERVICE_HOST"));
        node.setPort("9102");
        /* Following codes will cause an unregistration from MSB (due to MSB malfunction), comment them for now
        String msbAddrTemplate = (CommonUtils.isHttpsEnabled() ? "https" : "http")
                + "://%s:%s/api/holmes-engine-mgmt/v1/healthcheck";
        node.setCheckType("HTTP");
        node.setCheckUrl(String.format(msbAddrTemplate, serviceAddrInfo[0], "9102"));
        node.setCheckTimeOut("60s");
        node.setCheckInterval("60s");
        */
        nodes.add(node);
        msinfo.setNodes(nodes);
        return msinfo;
    }
}
