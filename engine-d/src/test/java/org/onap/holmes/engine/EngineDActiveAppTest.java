/**
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
package org.onap.holmes.engine;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.onap.msb.sdk.discovery.entity.Node;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.easymock.EasyMock.anyObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@PrepareForTest(MicroServiceConfig.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class EngineDActiveAppTest {

    public static void main(String[] args) throws Exception {
        String filePath = "C:\\engine-d.yml";
        new EngineDActiveApp().run(new String[]{"server", filePath});
    }

    @Test
    public void testCreateMicroServiceInfo() throws Exception {
        EngineDActiveApp engineDActiveApp = new EngineDActiveApp();
        PowerMock.mockStatic(MicroServiceConfig.class);
        String[] serviceAddrInfo = new String[2];
        serviceAddrInfo[0] = "10.74.216.82";
        serviceAddrInfo[1] = "80";
        EasyMock.expect(MicroServiceConfig.getMicroServiceIpAndPort()).andReturn(serviceAddrInfo);
        EasyMock.expectLastCall();
        EasyMock.expect(MicroServiceConfig.getEnv(anyObject(String.class))).andReturn("true").times(2);
        PowerMock.replayAll();

        MicroServiceInfo msinfo = Whitebox.invokeMethod(engineDActiveApp,"createMicroServiceInfo");

        PowerMock.verifyAll();

        assertThat(msinfo.getServiceName(), equalTo("holmes-engine-mgmt"));
        assertThat(msinfo.getVersion(), equalTo("v1"));
        assertThat(msinfo.getUrl(), equalTo("/api/holmes-engine-mgmt/v1"));
        assertThat(msinfo.getProtocol(), equalTo("REST"));
        assertThat(msinfo.getVisualRange(), equalTo("0|1"));
        assertThat(msinfo.isEnable_ssl(), is(true));
        assertThat(msinfo.getNodes().toArray(new Node[0])[0].getIp(), equalTo(serviceAddrInfo[0]));
        assertThat(msinfo.getNodes().toArray(new Node[0])[0].getPort(), equalTo("9102"));
    }
}
