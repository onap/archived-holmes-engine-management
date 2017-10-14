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
package org.onap.holmes.engine.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class CorrelationRuleMapper implements ResultSetMapper<CorrelationRule> {

    public CorrelationRule map(int i, ResultSet resultSet, StatementContext statementContext)
        throws SQLException {
        CorrelationRule correlationRule = new CorrelationRule();
        correlationRule.setName(resultSet.getString("name"));
        correlationRule.setRid(resultSet.getString("rid"));
        correlationRule.setDescription(resultSet.getString("description"));
        correlationRule.setEnabled(resultSet.getInt("enable"));
        correlationRule.setTemplateID(resultSet.getInt("templateID"));
        correlationRule.setEngineID(resultSet.getString("engineID"));
        correlationRule.setEngineType(resultSet.getString("engineType"));
        correlationRule.setCreator(resultSet.getString("creator"));
        correlationRule.setCreateTime(resultSet.getDate("createTime"));
        correlationRule.setModifier(resultSet.getString("updator"));
        correlationRule.setUpdateTime(resultSet.getDate("updateTime"));
        correlationRule.setParams((Properties) resultSet.getObject("params"));
        correlationRule.setContent(resultSet.getString("content"));
        correlationRule.setVendor(resultSet.getString("vendor"));
        correlationRule.setPackageName(resultSet.getString("package"));
        correlationRule.setClosedControlLoopName(resultSet.getString("ctrlloop"));
        return correlationRule;
    }

}
