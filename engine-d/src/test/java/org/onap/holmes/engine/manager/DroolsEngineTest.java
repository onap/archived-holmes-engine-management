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
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.conf.EventProcessingOption;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.constant.AlarmConst;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.engine.wrapper.RuleMgtWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;

public class DroolsEngineTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    private RuleMgtWrapper ruleMgtWrapper;

    private KnowledgeBase kbase;

    private KnowledgeBaseConfiguration kconf;

    private StatefulKnowledgeSession ksession;


    private DroolsEngine droolsEngine;

    @Before
    public void setUp() {
        droolsEngine = new DroolsEngine();

        this.kconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        this.kconf.setOption(EventProcessingOption.STREAM);
        this.kconf.setProperty("drools.assertBehaviour", "equality");
        this.kbase = KnowledgeBaseFactory.newKnowledgeBase("D-ENGINE", this.kconf);
        this.ksession = kbase.newStatefulKnowledgeSession();

        ruleMgtWrapper = PowerMock.createMock(RuleMgtWrapper.class);

        Whitebox.setInternalState(droolsEngine, "ruleMgtWrapper", ruleMgtWrapper);

        Whitebox.setInternalState(droolsEngine, "kconf", kconf);
        Whitebox.setInternalState(droolsEngine, "kbase", kbase);
        Whitebox.setInternalState(droolsEngine, "ksession", ksession);

        PowerMock.resetAll();
    }

    @Test
    public void init() throws Exception {

        List<CorrelationRule> rules = new ArrayList<CorrelationRule>();
        CorrelationRule rule = new CorrelationRule();
        rule.setContent("content");
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
    public void putRaisedIntoStream_facthandle_is_null() {
        VesAlarm raiseAlarm = new VesAlarm();
        droolsEngine.putRaisedIntoStream(raiseAlarm);
        droolsEngine.putRaisedIntoStream(raiseAlarm);
    }

    @Test
    public void putRaisedIntoStream_factHandle_is_not_null() {
        droolsEngine.putRaisedIntoStream(new VesAlarm());
    }

    @Test
    public void stop() throws Exception {
        droolsEngine.stop();
    }
}
