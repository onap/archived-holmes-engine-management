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

package org.openo.holmes.engine.wrapper;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.DbDaoUtil;
import org.openo.holmes.engine.db.CorrelationRuleDao;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class RuleMgtWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private DbDaoUtil daoUtil;
    private RuleMgtWrapper ruleMgtWrapper;

    @Before
    public void setUp() {
        daoUtil = PowerMock.createMock(DbDaoUtil.class);
        ruleMgtWrapper = new RuleMgtWrapper();

        Whitebox.setInternalState(ruleMgtWrapper, "daoUtil", daoUtil);
        PowerMock.resetAll();
    }

    @Test
    public void queryRuleByEnable_ruletemp_is_null() throws CorrelationException {
        int enable = 3;

        thrown.expect(CorrelationException.class);

        CorrelationRuleDao correlationRuleDao = PowerMock.createMock(CorrelationRuleDao.class);
        expect(daoUtil.getJdbiDaoByOnDemand(anyObject(Class.class))).andReturn(correlationRuleDao);
        expect(correlationRuleDao.queryRuleByRuleEnable(anyInt())).andReturn(null);
        PowerMock.replayAll();
        ruleMgtWrapper.queryRuleByEnable(enable);
        PowerMock.verifyAll();
    }

    @Test
    public void queryRuleByEnable_normal() throws CorrelationException {
        int enable = 3;

        CorrelationRuleDao correlationRuleDao = PowerMock.createMock(CorrelationRuleDao.class);
        expect(daoUtil.getJdbiDaoByOnDemand(anyObject(Class.class))).andReturn(correlationRuleDao);
        expect(correlationRuleDao.queryRuleByRuleEnable(anyInt())).andReturn(new ArrayList<CorrelationRule>());
        PowerMock.replayAll();
        ruleMgtWrapper.queryRuleByEnable(enable);
        PowerMock.verifyAll();
    }
}
