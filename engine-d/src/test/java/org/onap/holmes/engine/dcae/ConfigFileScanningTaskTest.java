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

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.dcae.DcaeConfigurationsCache;
import org.onap.holmes.common.utils.SpringContextUtil;
import org.onap.holmes.dsa.dmaappolling.DMaaPResponseUtil;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.dmaap.SubscriberAction;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringContextUtil.class})
public class ConfigFileScanningTaskTest {

    @Test
    public void run() {
        PowerMock.mockStatic(SpringContextUtil.class);
        SubscriberAction mockedSa = PowerMock.createMock(SubscriberAction.class);
        EasyMock.expect(SpringContextUtil.getBean(SubscriberAction.class)).andReturn(mockedSa);
        // This is invoked while executing new Subscriber().
        EasyMock.expect(SpringContextUtil.getBean(DMaaPResponseUtil.class)).andReturn(new DMaaPResponseUtil());
        mockedSa.addSubscriber(EasyMock.anyObject(Subscriber.class));
        EasyMock.expectLastCall();

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(null);
        String configFilePath = ConfigFileScanningTaskTest.class.getResource("/cfy.json").getFile();
        Whitebox.setInternalState(cfst, "configFile", configFilePath);

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(DcaeConfigurationsCache.getPubSecInfo("dcae_cl_out").getDmaapInfo().getTopicUrl(),
                equalTo("http://message-router.onap:3904/events/unauthenticated.DCAE_CL_OUTPUT"));
    }

    @Test
    public void run_config_not_changed() {
        PowerMock.mockStatic(SpringContextUtil.class);
        SubscriberAction mockedSa = PowerMock.createMock(SubscriberAction.class);
        // mocked objects will be only used once
        EasyMock.expect(SpringContextUtil.getBean(SubscriberAction.class)).andReturn(mockedSa);
        // This is invoked while executing new Subscriber().
        EasyMock.expect(SpringContextUtil.getBean(DMaaPResponseUtil.class)).andReturn(new DMaaPResponseUtil());
        mockedSa.addSubscriber(EasyMock.anyObject(Subscriber.class));
        EasyMock.expectLastCall();

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(null);
        String configFilePath = ConfigFileScanningTaskTest.class.getResource("/cfy.json").getFile();
        Whitebox.setInternalState(cfst, "configFile", configFilePath);

        PowerMock.replayAll();
        cfst.run();
        cfst.run();
        PowerMock.verifyAll();
    }
}