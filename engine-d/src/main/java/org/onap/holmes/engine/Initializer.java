/**
 * Copyright 2017-2018 ZTE Corporation.
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

import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.CommonUtils;
import org.onap.holmes.common.utils.MsbRegister;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.onap.msb.sdk.discovery.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.onap.holmes.common.config.MicroServiceConfig.getMicroServiceIpAndPort;
import static org.onap.holmes.common.utils.CommonUtils.getEnv;
import static org.onap.holmes.common.utils.CommonUtils.isIpAddress;

@Service
public class Initializer {
    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
    private MsbRegister msbRegister;

    @Inject
    public Initializer(MsbRegister msbRegister) {
        this.msbRegister = msbRegister;
    }

    @PostConstruct
    private void init() {
        try {
            msbRegister.register2Msb(createMicroServiceInfo());
        } catch (CorrelationException e) {
            logger.error(e.getMessage(), e);
        }
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
