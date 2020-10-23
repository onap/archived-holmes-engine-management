/**
 * Copyright 2020 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.holmes.engine;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.utils.MsbRegister;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MicroServiceConfig.class)
@SuppressStaticInitializationFor("org.onap.holmes.common.utils.HttpsUtils")
public class InitializerTest {

    @Test
    public void process() throws Exception {
        MsbRegister mockedMsbRegister = PowerMock.createMock(MsbRegister.class);
        Initializer initializer = new Initializer(mockedMsbRegister);

        PowerMock.mockStaticPartial(MicroServiceConfig.class, "getMicroServiceIpAndPort", "getEnv");
        EasyMock.expect(MicroServiceConfig.getMicroServiceIpAndPort()).andReturn(new String[]{"127.0.0.1", "443"});
        EasyMock.expect(MicroServiceConfig.getEnv("ENABLE_ENCRYPT")).andReturn("true");

        mockedMsbRegister.register2Msb(EasyMock.anyObject(MicroServiceInfo.class));
        EasyMock.expectLastCall();

        PowerMock.replayAll();

        WhiteboxImpl.invokeMethod(initializer, "init");

        PowerMock.verifyAll();
    }
}