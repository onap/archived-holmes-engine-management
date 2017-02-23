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
package org.openo.holmes.engine.wrapper;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.common.exception.DbException;
import org.openo.holmes.common.utils.DbDaoUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.engine.db.CorrelationRuleDao;


@Service
@Singleton
@Slf4j
public class RuleMgtWrapper {

    @Inject
    private DbDaoUtil daoUtil;

    public List<CorrelationRule> queryRuleByEnable(int enable) throws DbException {

        List<CorrelationRule> ruleTemp = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
            .queryRuleByRuleEnable(enable);
        if (ruleTemp == null) {
            throw new DbException(I18nProxy.RULE_MANAGEMENT_DB_ERROR);
        }
        return ruleTemp;
    }
}
