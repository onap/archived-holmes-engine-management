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
package org.openo.holmes.engine.db;

import org.easymock.EasyMock;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.powermock.api.easymock.PowerMock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/20.
 */
public class CorrelationRuleDaoTest {
    private CorrelationRuleDao correlationRuleDao;

    @Before
    public void setUp() {
        correlationRuleDao = PowerMock.createMock(CorrelationRuleDao.class);
    }

    @Test
    public void queryRuleByEnable() {
        int enable = 0;
        EasyMock.expect(correlationRuleDao.queryRuleByEnable(EasyMock.anyInt())).andReturn(new ArrayList<CorrelationRule>());
        PowerMock.replayAll();
        List<CorrelationRule> correlationRules = correlationRuleDao.queryRuleByEnable(enable);
        PowerMock.verifyAll();
        Assert.assertThat(correlationRules, IsNull.<List<CorrelationRule>>notNullValue());
    }

}
