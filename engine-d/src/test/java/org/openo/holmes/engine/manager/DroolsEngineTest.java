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

package org.openo.holmes.engine.manager;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.easymock.EasyMock;
import org.glassfish.hk2.api.IterableProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.common.api.stat.Alarm;
import org.openo.holmes.common.config.MQConfig;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.engine.request.DeployRuleRequest;
import org.openo.holmes.engine.wrapper.RuleMgtWrapper;
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

    private KnowledgeBuilder kbuilder;

    private IterableProvider<MQConfig> mqConfigProvider;

    private ConnectionFactory connectionFactory;

    private DroolsEngine droolsEngine;

    @Before
    public void setUp() {
        droolsEngine = new DroolsEngine();

        ruleMgtWrapper = PowerMock.createMock(RuleMgtWrapper.class);
        kbase = PowerMock.createMock(KnowledgeBase.class);
        kconf = PowerMock.createMock(KnowledgeBaseConfiguration.class);
        ksession = PowerMock.createMock(StatefulKnowledgeSession.class);
        kbuilder = PowerMock.createMock(KnowledgeBuilder.class);
        mqConfigProvider = PowerMock.createMock(IterableProvider.class);
        connectionFactory = PowerMock.createMock(ConnectionFactory.class);

        Whitebox.setInternalState(droolsEngine, "ruleMgtWrapper", ruleMgtWrapper);
        Whitebox.setInternalState(droolsEngine, "kbase", kbase);
        Whitebox.setInternalState(droolsEngine, "kconf", kconf);
        Whitebox.setInternalState(droolsEngine, "ksession", ksession);
        Whitebox.setInternalState(droolsEngine, "kbuilder", kbuilder);
        Whitebox.setInternalState(droolsEngine, "mqConfigProvider", mqConfigProvider);
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
        DeployRuleRequest rule = PowerMock.createMock(DeployRuleRequest.class);
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(CorrelationException.class);

        KnowledgeBuilderErrors errors = PowerMock.createMock(KnowledgeBuilderErrors.class);
        expect(rule.getContent()).andReturn("rule");
        kbuilder.add(anyObject(Resource.class), anyObject(ResourceType.class));
        expect(kbuilder.hasErrors()).andReturn(true);
        expect(kbuilder.getErrors()).andReturn(errors);

        PowerMock.replayAll();

        droolsEngine.deployRule(rule, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void deployRule_kbase_knowledgePackages_contains_package() throws CorrelationException {
        DeployRuleRequest rule = PowerMock.createMock(DeployRuleRequest.class);
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(CorrelationException.class);

        KnowledgePackage kPackage = PowerMock.createMock(KnowledgePackage.class);
        Collection<KnowledgePackage> builderColl = PowerMock.createMock(Collection.class);
        Iterator<KnowledgePackage> iterator = PowerMock.createMock(Iterator.class);
        Collection<KnowledgePackage> baseColl = new ArrayList<KnowledgePackage>();
        baseColl.add(kPackage);
        expect(rule.getContent()).andReturn("rule");
        expect(kbuilder.hasErrors()).andReturn(false);
        kbuilder.add(anyObject(Resource.class), anyObject(ResourceType.class));
        expect(kbuilder.getKnowledgePackages()).andReturn(builderColl);
        expect(builderColl.iterator()).andReturn(iterator);
        expect(iterator.next()).andReturn(kPackage);
        expect(kbase.getKnowledgePackages()).andReturn(baseColl);

        PowerMock.replayAll();

        droolsEngine.deployRule(rule, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void deployRule_add_knowledge_packages_exception() throws CorrelationException {
        DeployRuleRequest rule = PowerMock.createMock(DeployRuleRequest.class);
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(CorrelationException.class);

        KnowledgePackage kPackage = PowerMock.createMock(KnowledgePackage.class);
        Collection<KnowledgePackage> builderColl = PowerMock.createMock(Collection.class);
        Iterator<KnowledgePackage> iterator = PowerMock.createMock(Iterator.class);
        Collection<KnowledgePackage> baseColl = new ArrayList<KnowledgePackage>();
        expect(rule.getContent()).andReturn("rule");
        expect(kbuilder.hasErrors()).andReturn(false);
        kbuilder.add(anyObject(Resource.class), anyObject(ResourceType.class));
        expect(kbuilder.getKnowledgePackages()).andReturn(builderColl).times(2);
        expect(builderColl.iterator()).andReturn(iterator);
        expect(iterator.next()).andReturn(kPackage);
        expect(kbase.getKnowledgePackages()).andReturn(baseColl);
        kbase.addKnowledgePackages(anyObject(Collection.class));
        EasyMock.expectLastCall().andThrow(new RuntimeException(""));

        PowerMock.replayAll();

        droolsEngine.deployRule(rule, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void undeployRule_knowledgepackage_is_null() throws CorrelationException {
        String packageName = "packageName";
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(CorrelationException.class);

        expect(kbase.getKnowledgePackage(anyObject(String.class))).andReturn(null);

        PowerMock.replayAll();

        droolsEngine.undeployRule(packageName, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void undeployRule_remove_knowledge_package_exception() throws CorrelationException {
        String packageName = "packageName";
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(CorrelationException.class);

        KnowledgePackage pkg = PowerMock.createMock(KnowledgePackage.class);
        expect(kbase.getKnowledgePackage(anyObject(String.class))).andReturn(pkg);
        expect(pkg.getName()).andReturn("");
        kbase.removeKnowledgePackage(anyObject(String.class));
        EasyMock.expectLastCall().andThrow(new RuntimeException(""));

        PowerMock.replayAll();

        droolsEngine.undeployRule(packageName, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void undeployRule_normal() throws CorrelationException {
        String packageName = "packageName";
        Locale locale = PowerMock.createMock(Locale.class);

        KnowledgePackage pkg = PowerMock.createMock(KnowledgePackage.class);
        expect(kbase.getKnowledgePackage(anyObject(String.class))).andReturn(pkg);
        expect(pkg.getName()).andReturn("");
        kbase.removeKnowledgePackage(anyObject(String.class));

        PowerMock.replayAll();

        droolsEngine.undeployRule(packageName, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void compileRule_kbuilder_has_errors() throws CorrelationException {
        String content = "content";
        Locale locale = PowerMock.createMock(Locale.class);

        thrown.expect(CorrelationException.class);

        KnowledgeBuilderErrors errors = PowerMock.createMock(KnowledgeBuilderErrors.class);
        kbuilder.add(anyObject(Resource.class), anyObject(ResourceType.class));
        expect(kbuilder.hasErrors()).andReturn(true);
        expect(kbuilder.getErrors()).andReturn(errors);

        PowerMock.replayAll();

        droolsEngine.compileRule(content, locale);

        PowerMock.verifyAll();
    }

    @Test
    public void putRaisedIntoStream_facthandle_is_null() {
        expect(ksession.getFactHandle(anyObject(Alarm.class))).andReturn(null);
        expect(ksession.insert(anyObject(Alarm.class))).andReturn(null);
        expect(ksession.fireAllRules()).andReturn(0);

        PowerMock.replayAll();

        droolsEngine.putRaisedIntoStream(new Alarm());

        PowerMock.verifyAll();
    }

    @Test
    public void putRaisedIntoStream_factHandle_is_not_null() {
        FactHandle factHandle = PowerMock.createMock(FactHandle.class);
        expect(ksession.getFactHandle(anyObject(Alarm.class))).andReturn(factHandle);
        ksession.retract(anyObject(FactHandle.class));
        expect(ksession.insert(anyObject(Alarm.class))).andReturn(null);
        expect(ksession.fireAllRules()).andReturn(0);

        PowerMock.replayAll();

        droolsEngine.putRaisedIntoStream(new Alarm());

        PowerMock.verifyAll();
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

        expect(ksession.getFactHandle(anyObject(Alarm.class))).andReturn(null);

        expect(ksession.insert(anyObject(Alarm.class))).andReturn(null);
        expect(ksession.fireAllRules()).andReturn(1);

        PowerMock.replayAll();

        listener.onMessage(objectMessage);

        PowerMock.verifyAll();
    }

    @Test
    public void stop() throws Exception {
        droolsEngine.stop();
    }
}
