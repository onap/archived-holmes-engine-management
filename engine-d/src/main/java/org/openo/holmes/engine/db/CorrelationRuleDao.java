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
package org.openo.holmes.engine.db;


import java.util.List;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.engine.db.mapper.CorrelationRuleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(CorrelationRuleMapper.class)
public abstract class CorrelationRuleDao {


    @SqlQuery("SELECT * FROM aplus_rule WHERE enable=:enable")
    public abstract List<CorrelationRule> queryRuleByEnable(@Bind("enable") int enable);

    public List<CorrelationRule> queryRuleByRuleEnable(int enable) {
        return queryRuleByEnable(enable);
    }
}

