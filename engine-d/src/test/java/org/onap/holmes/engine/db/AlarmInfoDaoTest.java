/**
 * Copyright 2017-2021 ZTE Corporation.
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

package org.onap.holmes.engine.db;

import org.easymock.EasyMock;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.holmes.common.api.entity.AlarmInfo;
import org.powermock.api.easymock.PowerMock;

import java.util.ArrayList;
import java.util.List;

public class AlarmInfoDaoTest {


    private AlarmInfoDaoService alarmInfoDaoService;

    @Before
    public void setUp() {
        alarmInfoDaoService  = PowerMock.createMock(AlarmInfoDaoService.class);
    }

    @Test
    public void queryAllAlarm() throws Exception {

        EasyMock.expect(alarmInfoDaoService.queryAllAlarm()).andReturn(new ArrayList<AlarmInfo>());
        PowerMock.replayAll();

        List<AlarmInfo> alarmInfoList = alarmInfoDaoService.queryAllAlarm();
        PowerMock.verifyAll();
        Assert.assertThat(alarmInfoList, IsNull.<List<AlarmInfo>>notNullValue());
    }
}
