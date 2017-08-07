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

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.conf.EventProcessingOption;
import org.drools.runtime.StatefulKnowledgeSession;
import org.easymock.EasyMock;
import org.glassfish.hk2.api.IterableProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.api.stat.Alarm;
import org.onap.holmes.common.config.MQConfig;
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

    private IterableProvider<MQConfig> mqConfigProvider;

    private ConnectionFactory connectionFactory;

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
        mqConfigProvider = PowerMock.createMock(IterableProvider.class);
        connectionFactory = PowerMock.createMock(ConnectionFactory.class);

        Whitebox.setInternalState(droolsEngine, "ruleMgtWrapper", ruleMgtWrapper);
        Whitebox.setInternalState(droolsEngine, "mqConfigProvider", mqConfigProvider);
        Whitebox.setInternalState(droolsEngine, "kconf", kconf);
        Whitebox.setInternalState(droolsEngine, "kbase", kbase);
        Whitebox.setInternalState(droolsEngine, "ksession", ksession);
        Whitebox.setInternalState(droolsEngine, "connectionFactory", connectionFactory);

        PowerMock.resetAll();
    }

    @Test
    public void init() throws Exception {
        MQConfig mqConfig = new MQConfig();
        mqConfig.brokerIp = "127.0.0.1";
        mqConfig.brokerPort = 4567;
        mqConfig.brokerUsername = "admin";
        mqConfig.brokerPassword = "admin";
        List<CorrelationRule> rules = new ArrayList<CorrelationRule>();
        CorrelationRule rule = new CorrelationRule();
        rule.setContent("content");
        rules.add(rule);

        expect(mqConfigProvider.get()).andReturn(mqConfig).anyTimes();
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
        Alarm raiseAlarm = new Alarm();
        droolsEngine.putRaisedIntoStream(raiseAlarm);
        droolsEngine.putRaisedIntoStream(raiseAlarm);
    }

    @Test
    public void putRaisedIntoStream_factHandle_is_not_null() {
        droolsEngine.putRaisedIntoStream(new Alarm());
    }


    @Test
    public void listener_receive() throws JMSException {
        DroolsEngine.AlarmMqMessageListener listener = droolsEngine.new AlarmMqMessageListener();

        Connection connection = PowerMock.createMock(Connection.class);
        Session session = PowerMock.createMock(Session.class);
        Destination destination = PowerMock.createMock(Topic.class);
        MessageConsumer consumer = PowerMock.createMock(MessageConsumer.class);

        Whitebox.setInternalState(listener, "connection", connection);
        Whitebox.setInternalState(listener, "session", session);
        Whitebox.setInternalState(listener, "destination", destination);
        Whitebox.setInternalState(listener, "consumer", consumer);

        PowerMock.reset();

        expect(connectionFactory.createConnection()).andReturn(connection);
        connection.start();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session);
        expect(session.createTopic(anyObject(String.class))).andReturn((Topic) destination);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(consumer);
        consumer.setMessageListener(listener);

        PowerMock.replayAll();

        listener.receive();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_exception() throws JMSException {
        DroolsEngine.AlarmMqMessageListener listener = droolsEngine.new AlarmMqMessageListener();

        Connection connection = PowerMock.createMock(Connection.class);
        Session session = PowerMock.createMock(Session.class);
        Destination destination = PowerMock.createMock(Topic.class);
        MessageConsumer consumer = PowerMock.createMock(MessageConsumer.class);

        Whitebox.setInternalState(listener, "connection", connection);
        Whitebox.setInternalState(listener, "session", session);
        Whitebox.setInternalState(listener, "destination", destination);
        Whitebox.setInternalState(listener, "consumer", consumer);

        PowerMock.reset();

        expect(connectionFactory.createConnection()).andReturn(connection);
        connection.start();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session);
        expect(session.createTopic(anyObject(String.class))).andReturn((Topic) destination);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(consumer);
        consumer.setMessageListener(listener);
        EasyMock.expectLastCall().andThrow(new JMSException(""));

        consumer.close();
        session.close();
        connection.close();

        PowerMock.replayAll();

        listener.receive();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_close_exception() throws JMSException {
        DroolsEngine.AlarmMqMessageListener listener = droolsEngine.new AlarmMqMessageListener();

        Connection connection = PowerMock.createMock(Connection.class);
        Session session = PowerMock.createMock(Session.class);
        Destination destination = PowerMock.createMock(Topic.class);
        MessageConsumer consumer = PowerMock.createMock(MessageConsumer.class);

        Whitebox.setInternalState(listener, "connection", connection);
        Whitebox.setInternalState(listener, "session", session);
        Whitebox.setInternalState(listener, "destination", destination);
        Whitebox.setInternalState(listener, "consumer", consumer);

        PowerMock.reset();

        expect(connectionFactory.createConnection()).andReturn(connection);
        connection.start();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session);
        expect(session.createTopic(anyObject(String.class))).andReturn((Topic) destination);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(consumer);
        consumer.setMessageListener(listener);
        EasyMock.expectLastCall().andThrow(new JMSException(""));

        consumer.close();
        EasyMock.expectLastCall().andThrow(new JMSException(""));

        PowerMock.replayAll();

        listener.receive();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_on_message() throws JMSException {
        DroolsEngine.AlarmMqMessageListener listener = droolsEngine.new AlarmMqMessageListener();
        Alarm alarm = new Alarm();
        alarm.setAlarmKey("alarmKey");
        ActiveMQObjectMessage objectMessage = new ActiveMQObjectMessage();
        objectMessage.setObject(alarm);

        listener.onMessage(objectMessage);
    }

    @Test
    public void stop() throws Exception {
        droolsEngine.stop();
    }
}
