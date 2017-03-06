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


import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.common.api.stat.Alarm;
import org.openo.holmes.common.config.MQConfig;
import org.openo.holmes.common.constant.AlarmConst;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.ExceptionUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.engine.request.DeployRuleRequest;
import org.openo.holmes.engine.wrapper.RuleMgtWrapper;

@Slf4j
@Service
public class DroolsEngine {
    private final static int ENABLE = 1;

    @Inject
    private RuleMgtWrapper ruleMgtWrapper;

    private KnowledgeBase kbase;

    private KnowledgeBaseConfiguration kconf;

    private StatefulKnowledgeSession ksession;

    private KnowledgeBuilder kbuilder;

    @Inject
    private IterableProvider<MQConfig> mqConfigProvider;

    private ConnectionFactory connectionFactory;

    @PostConstruct
    private void init() {
        try {
            // 1. start engine
            start();
            // 2. start mq listener
            registerAlarmTopicListener();
        } catch (Exception e) {
            log.error("Start service failed: " + e.getMessage(), e);
            throw ExceptionUtil.buildExceptionResponse("Start service failed!");
        }
    }

    private void registerAlarmTopicListener() {
        String brokerURL =
            "tcp://" + mqConfigProvider.get().brokerIp + ":" + mqConfigProvider.get().brokerPort;
        connectionFactory = new ActiveMQConnectionFactory(mqConfigProvider.get().brokerUsername,
            mqConfigProvider.get().brokerPassword, brokerURL);

        AlarmMqMessageListener listener = new AlarmMqMessageListener();
        listener.receive();
    }


    private void start() throws CorrelationException {
        log.info("Drools Engine Initialize Beginning...");

        initEngineParameter();
        initDeployRule();

        log.info("Business Rule Engine Initialize Successfully.");
    }

    public void stop() {
        this.ksession.dispose();
    }

    private void initEngineParameter(){
        this.kconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();

        this.kconf.setOption(EventProcessingOption.STREAM);

        this.kconf.setProperty("drools.assertBehaviour", "equality");

        this.kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        this.kbase = KnowledgeBaseFactory.newKnowledgeBase("D-ENGINE", this.kconf);

        this.ksession = kbase.newStatefulKnowledgeSession();
    }

    private void initDeployRule() throws CorrelationException {
        List<CorrelationRule> rules = ruleMgtWrapper.queryRuleByEnable(ENABLE);

        if (rules.isEmpty()) {
            return;
        }
        for (CorrelationRule rule : rules) {
            if (rule.getContent() != null) {
                deployRuleFromDB(rule.getContent());
            }
        }
    }

    private void deployRuleFromDB(String ruleContent) throws CorrelationException {
        StringReader reader = new StringReader(ruleContent);
        Resource res = ResourceFactory.newReaderResource(reader);

        if (kbuilder == null) {
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        }

        kbuilder.add(res, ResourceType.DRL);

        try {

            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (Exception e) {
            throw new CorrelationException(e.getMessage(), e);
        }

        ksession.fireAllRules();
    }

    public synchronized String deployRule(DeployRuleRequest rule, Locale locale)
        throws CorrelationException {
        StringReader reader = new StringReader(rule.getContent());
        Resource res = ResourceFactory.newReaderResource(reader);

        if (kbuilder == null) {
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        }

        kbuilder.add(res, ResourceType.DRL);

        if (kbuilder.hasErrors()) {

            String errorMsg = I18nProxy.getInstance().getValueByArgs(locale,
                I18nProxy.ENGINE_CONTENT_ILLEGALITY,
                new String[]{kbuilder.getErrors().toString()});
            throw new CorrelationException(errorMsg);
        }

        KnowledgePackage kpackage = kbuilder.getKnowledgePackages().iterator().next();

        if (kbase.getKnowledgePackages().contains(kpackage)) {

            String errorMsg = I18nProxy.getInstance().getValueByArgs(locale,
                I18nProxy.ENGINE_CONTENT_ILLEGALITY,new String[]{
                    I18nProxy.getInstance().getValue(locale, I18nProxy.ENGINE_CONTAINS_PACKAGE)});

            throw new CorrelationException(errorMsg);
        }
        try {

            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (Exception e) {

            String errorMsg =
                I18nProxy.getInstance().getValue(locale, I18nProxy.ENGINE_DEPLOY_RULE_FAILED);
            throw new CorrelationException(errorMsg, e);
        }

        ksession.fireAllRules();
        return kpackage.getName();
    }

    public synchronized void undeployRule(String packageName, Locale locale)
        throws CorrelationException {

        KnowledgePackage pkg = kbase.getKnowledgePackage(packageName);

        if (null == pkg) {
            String errorMsg = I18nProxy.getInstance().getValueByArgs(locale,
                I18nProxy.ENGINE_DELETE_RULE_NULL,
                new String[]{packageName});
            throw new CorrelationException(errorMsg);
        }

        try {

            kbase.removeKnowledgePackage(pkg.getName());
        } catch (Exception e) {
            String errorMsg = I18nProxy.getInstance().getValueByArgs(locale,
                I18nProxy.ENGINE_DELETE_RULE_FAILED, new String[]{packageName});
            throw new CorrelationException(errorMsg, e);
        }
    }

    public void compileRule(String content, Locale locale)
        throws CorrelationException {
        StringReader reader = new StringReader(content);
        Resource res = ResourceFactory.newReaderResource(reader);

        if (kbuilder == null) {
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        }

        kbuilder.add(res, ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            String errorMsg = I18nProxy.getInstance().getValueByArgs(locale,
                I18nProxy.ENGINE_CONTENT_ILLEGALITY,
                new String[]{kbuilder.getErrors().toString()});
            log.error(errorMsg);
            throw new CorrelationException(errorMsg);
        }
    }

    public void putRaisedIntoStream(Alarm raiseAlarm) {
        FactHandle factHandle = this.ksession.getFactHandle(raiseAlarm);
        if (factHandle != null) {
            this.ksession.retract(factHandle);
        }
        this.ksession.insert(raiseAlarm);
        this.ksession.fireAllRules();
    }

    class AlarmMqMessageListener implements MessageListener {

        private Connection connection = null;
        private Session session = null;
        private Destination destination = null;
        private MessageConsumer consumer = null;

        private void initialize() throws JMSException {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createTopic(AlarmConst.MQ_TOPIC_NAME_ALARM);
            consumer = session.createConsumer(destination);
            connection.start();
        }

        public void receive() {
            try {
                initialize();
                consumer.setMessageListener(this);
            } catch (JMSException e) {
                log.error("Failed to connect to the MQ service : " + e.getMessage(), e);
                try {
                    close();
                } catch (JMSException e1) {
                    log.error("Failed close connection  " + e1.getMessage(), e1);
                }
            }
        }

        public void onMessage(Message arg0) {
            ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) arg0;
            try {
                Serializable object = objectMessage.getObject();

                if (object instanceof Alarm) {
                    Alarm alarm = (Alarm) object;
                    putRaisedIntoStream(alarm);
                }
            } catch (JMSException e) {
                log.error("Failed get object : " + e.getMessage(), e);
            }
        }

        private void close() throws JMSException {
            if (consumer != null)
                consumer.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        }
    }
}
