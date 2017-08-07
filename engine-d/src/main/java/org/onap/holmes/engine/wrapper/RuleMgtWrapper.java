/**
 * Copyright 2017 ZTE Corporation.
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
package org.onap.holmes.engine.wrapper;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.engine.db.CorrelationRuleDao;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;


@Service
@Singleton
@Slf4j
public class RuleMgtWrapper {

    @Inject
    private DbDaoUtil daoUtil;

    public List<CorrelationRule> queryRuleByEnable(int enable) throws CorrelationException {

        List<CorrelationRule> ruleTemp = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
            .queryRuleByRuleEnable(enable);
        return ruleTemp;
    }
}
