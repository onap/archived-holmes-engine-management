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
package org.onap.holmes.engine;

public class EngineDActiveAppTest {
    public static void main(String[] args) throws Exception {
        String test = "package org.onap.holmes.droolsRule;\n"
                + "\n"
                + "import org.onap.holmes.common.dmaap.DmaapService;\n"
                + "import org.onap.holmes.common.api.stat.VesAlarm;\n"
                + "import org.onap.holmes.common.aai.CorrelationUtil;\n"
                + "import org.onap.holmes.common.dmaap.entity.PolicyMsg;\n"
                + "import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;\n"
                + "import org.onap.holmes.common.utils.DroolsLog;\n"
                + " \n"
                + "\n"
                + "rule \"Relation_analysis_Rule\"\n"
                + "salience 200\n"
                + "no-loop true\n"
                + "    when\n"
                + "        $root : VesAlarm(alarmIsCleared == 0,\n"
                + "            $sourceId: sourceId, sourceId != null && !sourceId.equals(\"\"),\n"
                + "\t\t\t$sourceName: sourceName, sourceName != null && !sourceName.equals(\"\"),\n"
                + "\t\t\t$startEpochMicrosec: startEpochMicrosec,\n"
                + "            specificProblem in (\"Fault_MultiCloud_VMFailure\"),\n"
                + "            $eventId: eventId)\n"
                + "        $child : VesAlarm( eventId != $eventId, parentId == null,\n"
                + "            CorrelationUtil.getInstance().isTopologicallyRelated(sourceId, $sourceId, $sourceName),\n"
                + "            specificProblem in (\"Slave MPU is offline\"),\n"
                + "            startEpochMicrosec < $startEpochMicrosec + 60000 && startEpochMicrosec > $startEpochMicrosec - 60000 )\n"
                + "    then\n"
                + "\t\tDroolsLog.printInfo(\"===========================================================\");\n"
                + "\t\tDroolsLog.printInfo(\"Relation_analysis_Rule: rootId=\" + $root.getEventId() + \", childId=\" + $child.getEventId());\n"
                + "\t\t$child.setParentId($root.getEventId());\n"
                + "\t\tupdate($child);\n"
                + "\t\t\n"
                + "end\n"
                + "\n"
                + "rule \"root_has_child_handle_Rule\"\n"
                + "salience 150\n"
                + "no-loop true\n"
                + "\twhen\n"
                + "\t\t$root : VesAlarm(alarmIsCleared == 0, rootFlag == 0, $eventId: eventId)\n"
                + "\t\t$child : VesAlarm(eventId != $eventId, parentId == $eventId)\n"
                + "\tthen\n"
                + "\t\tDroolsLog.printInfo(\"===========================================================\");\n"
                + "\t\tDroolsLog.printInfo(\"root_has_child_handle_Rule: rootId=\" + $root.getEventId() + \", childId=\" + $child.getEventId());\n"
                + "\t\tDmaapService dmaapService = ServiceLocatorHolder.getLocator().getService(DmaapService.class);\n"
                + "\t\tPolicyMsg policyMsg = dmaapService.getPolicyMsg($root, $child, \"org.onap.holmes.droolsRule\");\n"
                + "        dmaapService.publishPolicyMsg(policyMsg, \"unauthenticated.DCAE_CL_OUTPUT\");\n"
                + "\t\t$root.setRootFlag(1);\n"
                + "\t\tupdate($root);\n"
                + "end\n"
                + "\n"
                + "rule \"root_no_child_handle_Rule\"\n"
                + "salience 100\n"
                + "no-loop true\n"
                + "    when\n"
                + "        $root : VesAlarm(alarmIsCleared == 0, rootFlag == 0,\n"
                + "            sourceId != null && !sourceId.equals(\"\"),\n"
                + "\t\t\tsourceName != null && !sourceName.equals(\"\"),\n"
                + "            specificProblem in (\"Fault_MultiCloud_VMFailure\"))\n"
                + "    then\n"
                + "\t\tDroolsLog.printInfo(\"===========================================================\");\n"
                + "\t\tDroolsLog.printInfo(\"root_no_child_handle_Rule: rootId=\" + $root.getEventId());\n"
                + "\t\tDmaapService dmaapService = ServiceLocatorHolder.getLocator().getService(DmaapService.class);\n"
                + "\t\tPolicyMsg policyMsg = dmaapService.getPolicyMsg($root, null, \"org.onap.holmes.droolsRule\");\n"
                + "        dmaapService.publishPolicyMsg(policyMsg, \"unauthenticated.DCAE_CL_OUTPUT\");\n"
                + "\t\t$root.setRootFlag(1);\n"
                + "\t\tupdate($root);\n"
                + "end\n"
                + "\n"
                + "rule \"root_cleared_handle_Rule\"\n"
                + "salience 100\n"
                + "no-loop true\n"
                + "    when\n"
                + "        $root : VesAlarm(alarmIsCleared == 1, rootFlag == 1)\n"
                + "    then\n"
                + "\t\tDroolsLog.printInfo(\"===========================================================\");\n"
                + "\t\tDroolsLog.printInfo(\"root_cleared_handle_Rule: rootId=\" + $root.getEventId());\n"
                + "\t\tDmaapService dmaapService = ServiceLocatorHolder.getLocator().getService(DmaapService.class);\n"
                + "\t\tPolicyMsg policyMsg = dmaapService.getPolicyMsg($root, null, \"org.onap.holmes.droolsRule\");\n"
                + "        dmaapService.publishPolicyMsg(policyMsg, \"unauthenticated.DCAE_CL_OUTPUT\");\n"
                + "\t\tretract($root);\n"
                + "end\n"
                + "\n"
                + "rule \"child_handle_Rule\"\n"
                + "salience 100\n"
                + "no-loop true\n"
                + "    when\n"
                + "        $child : VesAlarm(alarmIsCleared == 1, rootFlag == 0)\n"
                + "    then\n"
                + "\t\tDroolsLog.printInfo(\"===========================================================\");\n"
                + "\t\tDroolsLog.printInfo(\"child_handle_Rule: childId=\" + $child.getEventId());\n"
                + "\t\tretract($child);\n"
                + "end";
        String filePath = "E:\\项目代码\\ONAP\\holmes\\engine-management\\engine-d-standalone\\src\\main\\assembly\\conf\\engine-d.yml";
        new EngineDActiveApp().run(new String[]{"server", filePath});
    }
}
