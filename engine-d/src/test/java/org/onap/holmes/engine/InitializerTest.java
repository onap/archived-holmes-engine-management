/**
 * Copyright 2020-2023 ZTE Corporation.
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
import org.onap.holmes.common.msb.MsbRegister;
import org.onap.holmes.common.msb.entity.MicroServiceInfo;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MicroServiceConfig.class)
public class InitializerTest {

    @Test
    public void process() throws Exception {
        MsbRegister mockedMsbRegister = PowerMock.createMock(MsbRegister.class);
        Initializer initializer = new Initializer(mockedMsbRegister);

        PowerMock.mockStaticPartial(MicroServiceConfig.class, "getMicroServiceIpAndPort", "getEnv");
        EasyMock.expect(MicroServiceConfig.getMicroServiceIpAndPort()).andReturn(new String[]{"127.0.0.1", "443"});
        System.setProperty("ENABLE_ENCRYPT", "true");

        mockedMsbRegister.register2Msb(EasyMock.anyObject(MicroServiceInfo.class));
        EasyMock.expectLastCall();

        PowerMock.replayAll();

        setReadyFlagAfter(3);

        initializer.run(null);

        TimeUnit.SECONDS.sleep(6);

        PowerMock.verifyAll();
    }

    private void setReadyFlagAfter(final int second) {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(second);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Initializer.setReadyForMsbReg(true);
        }).start();
    }
}