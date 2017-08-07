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


import java.io.Serializable;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.api.stat.Alarm;
import org.onap.holmes.common.config.MQConfig;
import org.onap.holmes.common.constant.AlarmConst;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.ExceptionUtil;
import org.onap.holmes.engine.wrapper.RuleMgtWrapper;

@Slf4j
@Service
public class DroolsEngine {

    private final static int ENABLE = 1;
    private final Set<String> packageNames = new HashSet<String>();
    @Inject
    private RuleMgtWrapper ruleMgtWrapper;
    private KnowledgeBase kbase;
    private KnowledgeBaseConfiguration kconf;
    private StatefulKnowledgeSession ksession;
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
            log.error("Failed to start the service: " + e.getMessage(), e);
            throw ExceptionUtil.buildExceptionResponse("Failed to start the drools engine!");
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

    private void initEngineParameter() {
        this.kconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();

        this.kconf.setOption(EventProcessingOption.STREAM);

        this.kconf.setProperty("drools.assertBehaviour", "equality");

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

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

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

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(res, ResourceType.DRL);

        judgeRuleContent(locale, kbuilder, true);

        String packageName = kbuilder.getKnowledgePackages().iterator().next().getName();
        try {
            packageNames.add(packageName);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (Exception e) {
            throw new CorrelationException("Failed to deploy the rule.", e);
        }

        ksession.fireAllRules();
        return packageName;
    }

    public synchronized void undeployRule(String packageName, Locale locale)
        throws CorrelationException {

        KnowledgePackage pkg = kbase.getKnowledgePackage(packageName);

        if (null == pkg) {
            throw new CorrelationException("The rule " + packageName + " does not exist!");
        }

        try {
            kbase.removeKnowledgePackage(pkg.getName());
        } catch (Exception e) {
            throw new CorrelationException("Failed to delete the rule: " + packageName, e);
        }
    }

    public void compileRule(String content, Locale locale)
        throws CorrelationException {
        StringReader reader = new StringReader(content);
        Resource res = ResourceFactory.newReaderResource(reader);

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(res, ResourceType.DRL);

        judgeRuleContent(locale, kbuilder, false);
    }

    private void judgeRuleContent(Locale locale, KnowledgeBuilder kbuilder, boolean judgePackageName)
        throws CorrelationException {
        if (kbuilder.hasErrors()) {
            String errorMsg = "There are errors in the rule: " + kbuilder.getErrors().toString();
            log.error(errorMsg);
            throw new CorrelationException(errorMsg);
        }

        String packageName = kbuilder.getKnowledgePackages().iterator().next().getName();

        if (packageNames.contains(packageName) && judgePackageName) {
            throw new CorrelationException("The rule " + packageName + " already exists in the drools engine.");
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
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
