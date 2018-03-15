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

package org.onap.holmes.engine.manager;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.expect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.constant.AlarmConst;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.engine.wrapper.RuleMgtWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class DroolsEngineTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RuleMgtWrapper ruleMgtWrapper;

    private KieBase kieBase;
    private KieSession kieSession;
    private KieContainer kieContainer;
    private KieFileSystem kfs;
    private KieServices ks;
    private KieBuilder kieBuilder;
    private KieResources resources;
    private KieRepository kieRepository;


    private DroolsEngine droolsEngine;

    @Before
    public void setUp() throws Exception {
        droolsEngine = new DroolsEngine();

        ks = KieServices.Factory.get();
        resources = ks.getResources();
        kieRepository = ks.getRepository();
        kfs = Whitebox.invokeMethod(droolsEngine, "createKieFileSystemWithKProject", ks);
        kieBuilder = ks.newKieBuilder(kfs).buildAll();
        kieContainer = ks.newKieContainer(kieRepository.getDefaultReleaseId());
        kieBase = kieContainer.getKieBase();
        kieSession = kieContainer.newKieSession();

        ruleMgtWrapper = PowerMock.createMock(RuleMgtWrapper.class);

        Whitebox.setInternalState(droolsEngine, "ruleMgtWrapper", ruleMgtWrapper);

        Whitebox.setInternalState(droolsEngine, "kieBase", kieBase);
        Whitebox.setInternalState(droolsEngine, "kieSession", kieSession);
        Whitebox.setInternalState(droolsEngine, "kieContainer", kieContainer);
        Whitebox.setInternalState(droolsEngine, "kfs", kfs);
        Whitebox.setInternalState(droolsEngine, "ks", ks);
        Whitebox.setInternalState(droolsEngine, "kieBuilder", kieBuilder);
        Whitebox.setInternalState(droolsEngine, "resources", resources);
        Whitebox.setInternalState(droolsEngine, "kieRepository", kieRepository);

        PowerMock.resetAll();
    }

    @Test
    public void init() throws Exception {

        List<CorrelationRule> rules = new ArrayList<CorrelationRule>();
        CorrelationRule rule = new CorrelationRule();
        rule.setContent("package content");
        rule.setClosedControlLoopName("test");
        rule.setPackageName("org.onap.holmes");
        rules.add(rule);

        expect(ruleMgtWrapper.queryRuleByEnable(anyInt())).andReturn(rules);
        PowerMock.replayAll();

        Method method = DroolsEngine.class.getDeclaredMethod("init");
        method.setAccessible(true);
        method.invoke(droolsEngine);

        PowerMock.verifyAll();
    }

    @Test
    public void deployRule_rule_is_null() throws CorrelationException {
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(NullPointerException.class);

        droolsEngine.deployRule(null, locale);
    }

    @Test
    public void deployRule_kbuilder_has_errors() throws CorrelationException {
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("rule123");
        Locale locale = new Locale(AlarmConst.I18N_EN);

        thrown.expect(CorrelationException.class);

        droolsEngine.deployRule(rule, locale);
    }

    @Test
    public void deployRule_package_name_repeat() throws CorrelationException {
        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("package rule123");
        Locale locale = new Locale(AlarmConst.I18N_EN);

        thrown.expect(CorrelationException.class);

        droolsEngine.deployRule(rule, locale);
        droolsEngine.deployRule(rule, locale);
    }

    @Test
    public void undeployRule_package_name_is_null() throws CorrelationException {
        String packageName = null;
        Locale locale = new Locale(AlarmConst.I18N_EN);

        thrown.expect(CorrelationException.class);

        droolsEngine.undeployRule(packageName, locale);
    }

    @Test
    public void undeployRule_normal() throws CorrelationException {
        Locale locale = new Locale(AlarmConst.I18N_EN);

        DeployRuleRequest rule = new DeployRuleRequest();
        rule.setContent("package rule123");
        droolsEngine.deployRule(rule, locale);

        String packageName = "rule123";

        droolsEngine.undeployRule(packageName, locale);
    }

    @Test
    public void compileRule_kbuilder_has_errors() throws CorrelationException {
        String content = "have error content";
        Locale locale = new Locale(AlarmConst.I18N_EN);

        thrown.expect(CorrelationException.class);

        droolsEngine.compileRule(content, locale);
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
}
