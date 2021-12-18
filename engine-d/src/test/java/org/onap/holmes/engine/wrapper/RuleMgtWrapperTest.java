/**
 * Copyright 2017-2021 ZTE Corporation.
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

package org.onap.holmes.engine.wrapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.engine.db.CorrelationRuleDaoService;
import org.powermock.api.easymock.PowerMock;

import java.util.ArrayList;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.expect;

public class RuleMgtWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private CorrelationRuleDaoService correlationRuleDaoService;
    private RuleMgtWrapper ruleMgtWrapper;

    @Before
    public void setUp() {
        correlationRuleDaoService = PowerMock.createMock(CorrelationRuleDaoService.class);
        ruleMgtWrapper = new RuleMgtWrapper(correlationRuleDaoService);

        PowerMock.resetAll();
    }

    @Test
    public void queryRuleByEnable_normal() throws CorrelationException {
        int enable = 3;
        expect(correlationRuleDaoService.queryRuleByRuleEnable(anyInt())).andReturn(new ArrayList());
        PowerMock.replayAll();
        ruleMgtWrapper.queryRuleByEnable(enable);
        PowerMock.verifyAll();
    }
}
