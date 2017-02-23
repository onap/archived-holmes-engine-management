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
package org.openo.holmes.engine.utils;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openo.holmes.common.api.stat.Alarm;
import org.openo.holmes.common.producer.MQProducer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/20.
 */
public class AlarmUtilTest {
    private AlarmUtil alarmUtil;
    private final Map<String, Map<String, Integer>> rootPriorityMap = new HashMap<String, Map<String, Integer>>();
    private final Map<String, String> saveRuleMsg = new HashMap<String, String>();

    @Before
    public void setUp() {
        alarmUtil = AlarmUtil.getInstance();
        Whitebox.setInternalState(alarmUtil,"rootPriorityMap",rootPriorityMap);
        Whitebox.setInternalState(alarmUtil,"saveRuleMsg",saveRuleMsg);
        PowerMock.resetAll();
    }

    @Test
    public void getInstance() {
        AlarmUtil instance = AlarmUtil.getInstance();
        Assert.assertThat(instance,IsNull.<AlarmUtil>notNullValue());
    }

    @Test
    public void equipTypeFilter_is_nullstr() {
        String probableCauseStr = "null";
        String equipType = "equipType";
        Alarm alarm = new Alarm();
        boolean filter = alarmUtil.equipTypeFilter(probableCauseStr, equipType, alarm);
        Assert.assertThat(filter, IsEqual.equalTo(true));
    }

    @Test
    public void equipTypeFilter_equals_alarm() {
        String probableCauseStr = "11,4567";
        String equipType = "ee,equipType";
        Alarm alarm = new Alarm();
        alarm.setProbableCause(4567);
        alarm.setEquipType("equipType");
        boolean filter = alarmUtil.equipTypeFilter(probableCauseStr, equipType, alarm);
        Assert.assertThat(filter, IsEqual.equalTo(true));
    }

    @Test
    public void equipTypeFilter_not_equals_alarm() {
        String probableCauseStr = "11,45";
        String equipType = "ee,equipType";
        Alarm alarm = new Alarm();
        alarm.setProbableCause(4567);
        alarm.setEquipType("equipType");
        boolean filter = alarmUtil.equipTypeFilter(probableCauseStr, equipType, alarm);
        Assert.assertThat(filter, IsEqual.equalTo(false));
    }

    @Test
    public void getPriority_rootprioritymap_containskey_ruleid() {
        String ruleId = "1";
        String probableCauseStr = "11,4567";
        String rootAlarmFeatureStr = "0,1";
        String equipTypeStr = "ee,equipType";
        Alarm alarm = new Alarm();

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("11-ee", 0);
        map.put("4567-equipType", 1);
        rootPriorityMap.put(ruleId, map);

        saveRuleMsg.put(ruleId, "11ee0");

        Integer priority = alarmUtil.getPriority(ruleId, probableCauseStr, rootAlarmFeatureStr, equipTypeStr, alarm);
        Assert.assertThat(priority,IsEqual.equalTo(0));
    }

    @Test
    public void getPriority_rootprioritymap_not_containskey_ruleid() {
        String ruleId = "1";
        String probableCauseStr = "11,4567";
        String rootAlarmFeatureStr = "0,1";
        String equipTypeStr = "ee,equipType";
        Alarm alarm = new Alarm();

        saveRuleMsg.put(ruleId, "11ee0");

        Integer priority = alarmUtil.getPriority(ruleId, probableCauseStr, rootAlarmFeatureStr, equipTypeStr, alarm);
        Assert.assertThat(priority,IsEqual.equalTo(0));
    }

    @Test
    public void getPriority_priority_is_not_null() {
        String ruleId = "1";
        String probableCauseStr = "11,4567";
        String rootAlarmFeatureStr = "1,1";
        String equipTypeStr = "ee,equipType";
        Alarm alarm = new Alarm();
        alarm.setProbableCause(11);
        alarm.setEquipType("ee");

        saveRuleMsg.put(ruleId, "11ee0");

        Integer priority = alarmUtil.getPriority(ruleId, probableCauseStr, rootAlarmFeatureStr, equipTypeStr, alarm);
        Assert.assertThat(priority,IsEqual.equalTo(1));
    }

    @Test
    public void getMqProducer() {
        MQProducer mqProducer = alarmUtil.getMqProducer();
        Assert.assertThat(mqProducer, IsNull.<MQProducer>notNullValue());
    }
}
