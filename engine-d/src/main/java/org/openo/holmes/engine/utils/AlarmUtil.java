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
package org.openo.holmes.engine.utils;

import java.util.HashMap;
import java.util.Map;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.stat.Alarm;
import org.openo.holmes.common.producer.MQProducer;

@Service
public class AlarmUtil {

    private final static AlarmUtil alarmUtil = new AlarmUtil();
    /**
     * Map<ruleId, <ProbableCause-EquipType, priority>>
     */
    private final Map<String, Map<String, Integer>> rootPriorityMap =
        new HashMap<String, Map<String, Integer>>();
    /**
     * Map<rule, ProbableCause+EquipType+priority>
     */
    private final Map<String, String> saveRuleMsg = new HashMap<String, String>();

    private AlarmUtil() {
    }

    public static AlarmUtil getInstance() {
        return alarmUtil;
    }

    public boolean equipTypeFilter(String probableCauseStr, String equipType, Alarm alarm) {
        if ("null".equals(probableCauseStr)) {
            return true;
        }
        String[] equipTypes = equipType.replace(" ", "").split(",");
        String[] probableCauseStrs = probableCauseStr.replace(" ", "").split(",");
        for (int i = 0; i < probableCauseStrs.length; i++) {
            if (alarm.getProbableCause() == Long.parseLong(probableCauseStrs[i])
                && alarm.getEquipType().equals(equipTypes[i])) {
                return true;
            }
        }
        return false;
    }

    public Integer getPriority(String ruleId, String probableCauseStr, String rootAlarmFeatureStr,
        String equipTypeStr, Alarm alarm) {
        if (rootPriorityMap.containsKey(ruleId)) {
            if (!saveRuleMsg.get(ruleId)
                .equals(probableCauseStr + equipTypeStr + rootAlarmFeatureStr)) {
                setPriority(ruleId, probableCauseStr, rootAlarmFeatureStr, equipTypeStr);
            }
        } else {
            setPriority(ruleId, probableCauseStr, rootAlarmFeatureStr, equipTypeStr);
        }

        Integer priority =
            rootPriorityMap.get(ruleId).get(alarm.getProbableCause() + "-" + alarm.getEquipType());
        if (priority == null) {
            priority = 0;
        }
        return priority;
    }

    private void setPriority(String ruleId, String probableCauseStr, String rootAlarmFeatureStr,
        String equipTypeStr) {
        saveRuleMsg.put(ruleId, probableCauseStr + equipTypeStr + rootAlarmFeatureStr);

        Map<String, Integer> map = new HashMap<String, Integer>();
        String[] probableCauseStrs = probableCauseStr.replace(" ", "").split(",");
        String[] rootAlarmFeatureStrs = rootAlarmFeatureStr.replace(" ", "").split(",");
        String[] equipTypes = equipTypeStr.replace(" ", "").split(",");
        for (int i = 0; i < rootAlarmFeatureStrs.length; i++) {
            map.put(probableCauseStrs[i] + "-" + equipTypes[i],
                Integer.parseInt(rootAlarmFeatureStrs[i]));
        }

        rootPriorityMap.put(ruleId, map);
    }

    public MQProducer getMqProducer() {
        MQProducer mqProducer = new MQProducer();
        return mqProducer;
    }
}
