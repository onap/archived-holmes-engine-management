/**
 * Copyright 2017 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.engine.manager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.api.entity.AlarmInfo;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.engine.db.AlarmInfoDao;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.engine.wrapper.RuleMgtWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DroolsEngineTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RuleMgtWrapper ruleMgtWrapper;

    private AlarmInfoDao alarmInfoDaoMock;

    private DroolsEngine droolsEngine;

    private DbDaoUtil dbDaoUtilStub;

    public DroolsEngineTest() throws Exception {
        droolsEngine = new DroolsEngine();
        ruleMgtWrapper = new RuleMgtWrapperStub();
        dbDaoUtilStub = new DbDaoUtilStub();
        Whitebox.setInternalState(droolsEngine, "daoUtil", dbDaoUtilStub);
        Whitebox.setInternalState(droolsEngine, "ruleMgtWrapper", ruleMgtWrapper);
        Whitebox.invokeMethod(droolsEngine, "init");
    }

    @Before
    public void setUp() throws Exception {
        PowerMock.resetAll();
    }

    @Test
    public void deployRule_rule_is_null() throws CorrelationException {
        thrown.expect(NullPointerException.class);
        droolsEngine.deployRule(null);
    }

    @Test
    public void deployRule_invalid_rule_no_pkg_name() throws CorrelationException {
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("rule123;");
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The package name can not be empty.");

        droolsEngine.deployRule(rule);
    }

    @Test
    public void deployRule_invalid_rule_illegal_contents() throws CorrelationException {
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("package rule123; a random string");
        thrown.expect(CorrelationException.class);

        droolsEngine.deployRule(rule);
    }

    @Test
    public void deployRule_package_name_repeat() throws CorrelationException {
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("package rule123");

        thrown.expect(CorrelationException.class);
        thrown.expectMessage("A rule with the same package name already exists in the system.");
        droolsEngine.deployRule(rule);
        droolsEngine.deployRule(rule);
    }

    @Test
    public void undeployRule_package_name_is_null() throws CorrelationException {
        String packageName = null;
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The package name should not be null.");

        droolsEngine.undeployRule(packageName);
    }

    @Test
    public void undeployRule_normal() throws CorrelationException {
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("package rule123");
        droolsEngine.deployRule(rule);
        droolsEngine.undeployRule("rule123");
    }

    @Test
    public void compileRule_compilation_failure() throws CorrelationException {
        String content = "invalid contents";

        thrown.expect(CorrelationException.class);

        droolsEngine.compileRule(content);
    }

    @Test
    public void compileRule_compilation_deployed_rule() throws CorrelationException {
        String content = "package deployed;";
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent(content);
        rule.setLoopControlName(UUID.randomUUID().toString());
        thrown.expect(CorrelationException.class);

        droolsEngine.deployRule(rule);
        droolsEngine.compileRule(content);
    }

    @Test
    public void compileRule_compilation_normal() throws CorrelationException {
        String content = "package deployed;";
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent(content);
        rule.setLoopControlName(UUID.randomUUID().toString());

        droolsEngine.compileRule(content);
    }

    @Test
    public void putRaisedIntoStream_facthandle_is_not_null() {
        VesAlarm raiseAlarm = new VesAlarm();
        raiseAlarm.setSourceId("11111");
        raiseAlarm.setEventName("alarm");
        droolsEngine.putRaisedIntoStream(raiseAlarm);
        droolsEngine.putRaisedIntoStream(raiseAlarm);
    }

    @Test
    public void putRaisedIntoStream_factHandle_is_null() {
        VesAlarm raiseAlarm = new VesAlarm();
        raiseAlarm.setSourceId("11111");
        raiseAlarm.setEventName("alarm");
        droolsEngine.putRaisedIntoStream(raiseAlarm);
    }

    @Test
    public void stop() throws Exception {
        droolsEngine.stop();
    }

    @Test
    public void testConvertAlarmInfo2VesAlarm() throws Exception {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setEventId("eventId");
        alarmInfo.setEventName("eventName");
        alarmInfo.setStartEpochMicroSec(1L);
        alarmInfo.setLastEpochMicroSec(1L);
        alarmInfo.setSourceId("sourceId");
        alarmInfo.setSourceName("sourceName");
        alarmInfo.setRootFlag(0);
        alarmInfo.setAlarmIsCleared(1);

        VesAlarm vesAlarm = Whitebox.invokeMethod(droolsEngine, "convertAlarmInfo2VesAlarm", alarmInfo);

        assertThat(vesAlarm.getAlarmIsCleared(), is(1));
        assertThat(vesAlarm.getSourceName(), equalTo("sourceName"));
        assertThat(vesAlarm.getSourceId(), equalTo("sourceId"));
        assertThat(vesAlarm.getStartEpochMicrosec(), is(1L));
        assertThat(vesAlarm.getLastEpochMicrosec(), is(1L));
        assertThat(vesAlarm.getEventName(), equalTo("eventName"));
        assertThat(vesAlarm.getEventId(), equalTo("eventId"));
        assertThat(vesAlarm.getRootFlag(), is(0));
    }

    @Test
    public void testConvertVesAlarm2AlarmInfo() throws Exception {
        VesAlarm vesAlarm = new VesAlarm();
        vesAlarm.setEventId("eventId");
        vesAlarm.setEventName("eventName");
        vesAlarm.setStartEpochMicrosec(1L);
        vesAlarm.setLastEpochMicrosec(1L);
        vesAlarm.setSourceId("sourceId");
        vesAlarm.setSourceName("sourceName");
        vesAlarm.setRootFlag(0);
        vesAlarm.setAlarmIsCleared(1);

        AlarmInfo alarmInfo = Whitebox.invokeMethod(droolsEngine, "convertVesAlarm2AlarmInfo", vesAlarm);

        assertThat(alarmInfo.getAlarmIsCleared(), is(1));
        assertThat(alarmInfo.getSourceName(), equalTo("sourceName"));
        assertThat(alarmInfo.getSourceId(), equalTo("sourceId"));
        assertThat(alarmInfo.getStartEpochMicroSec(), is(1L));
        assertThat(alarmInfo.getLastEpochMicroSec(), is(1L));
        assertThat(alarmInfo.getEventName(), equalTo("eventName"));
        assertThat(alarmInfo.getEventId(), equalTo("eventId"));
        assertThat(alarmInfo.getRootFlag(), is(0));
    }

    @Test
    public void testQueryPackagesFromEngine() throws CorrelationException {

        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("package packageCheck; rule \"test\" when eval(1==1) then System.out.println(1); end");
        rule.setLoopControlName(UUID.randomUUID().toString());

        droolsEngine.deployRule(rule);

        List<String> packages = droolsEngine.queryPackagesFromEngine();

        assertThat(packages.contains("packageCheck"), is(true));
    }
}

class RuleMgtWrapperStub extends RuleMgtWrapper {
    private List<CorrelationRule> rules;

    public RuleMgtWrapperStub() {
        rules = new ArrayList<>();
        CorrelationRule rule = new CorrelationRule();
        rule.setEnabled(1);
        rule.setContent("package org.onap.holmes;");
        rule.setPackageName("UT");
        rule.setClosedControlLoopName(UUID.randomUUID().toString());
        rules.add(rule);
    }

    public List<CorrelationRule> getRules() {
        return rules;
    }

    public void setRules(List<CorrelationRule> rules) {
        this.rules = rules;
    }

    @Override
    public List<CorrelationRule> queryRuleByEnable(int enabled) throws CorrelationException {
        return rules.stream().filter(rule -> rule.getEnabled() == enabled).collect(Collectors.toList());
    }
}

class AlarmInfoDaoStub extends AlarmInfoDao {

    private List<AlarmInfo> alarms;

    public AlarmInfoDaoStub() {
        alarms = new ArrayList<>();
        AlarmInfo info = new AlarmInfo();
        info.setEventId("eventId");
        info.setEventName("eventName");
        info.setStartEpochMicroSec(1L);
        info.setLastEpochMicroSec(1L);
        info.setSourceId("sourceId");
        info.setSourceName("sourceName");
        info.setRootFlag(0);
        info.setAlarmIsCleared(1);
        alarms.add(info);
    }

    @Override
    protected String addAlarm(AlarmInfo alarmInfo) {
        alarms.add(alarmInfo);
        return null;
    }

    @Override
    protected List<AlarmInfo> queryAlarm() {
        return alarms;
    }

    @Override
    protected int deleteAlarmByAlarmIsCleared(String alarmName, String sourceName, String sourceId) {
        return 1;
    }
}

class DbDaoUtilStub extends DbDaoUtil {
    private AlarmInfoDao dao = new AlarmInfoDaoStub();

    @Override
    public <T> T getJdbiDaoByOnDemand(Class<T> daoClazz) {

        return (T) dao;

    }
}
