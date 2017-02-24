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


import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
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
import org.openo.holmes.common.exception.DbException;
import org.openo.holmes.common.exception.EngineException;
import org.openo.holmes.common.exception.RuleIllegalityException;
import org.openo.holmes.common.utils.ExceptionUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.engine.request.DeployRuleRequest;
import org.openo.holmes.engine.wrapper.RuleMgtWrapper;

@Slf4j
@Service
public class DroolsEngine {

    private final static String CORRELATION_RULE = "CORRELATION_RULE";

    private final static String CORRELATION_ALARM = "CORRELATION_ALARM";

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
            log.error("Start service failed: " + e.getMessage());
            throw ExceptionUtil.buildExceptionResponse("Start service failed!");
        }
    }

    private void registerAlarmTopicListener() {
        String brokerURL =
            "tcp://" + mqConfigProvider.get().brokerIp + ":" + mqConfigProvider.get().brokerPort;
        connectionFactory = new ActiveMQConnectionFactory(mqConfigProvider.get().brokerUsername,
            mqConfigProvider.get().brokerPassword, brokerURL);

        Thread thread = new Thread(new AlarmMqMessageListener());
        thread.start();
    }


    private void start() throws EngineException, RuleIllegalityException, DbException {
        log.info("Drools Egine Initialize Begining ... ");

        initEngineParameter();
        initDeployRule();

        log.info("Business Rule Egine Initialize Successfully ");
    }

    public void stop() throws Exception {
        this.ksession.dispose();
    }

    private void initEngineParameter() throws EngineException {
        this.kconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();

        this.kconf.setOption(EventProcessingOption.STREAM);

        this.kconf.setProperty("drools.assertBehaviour", "equality");

        this.kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        this.kbase = KnowledgeBaseFactory.newKnowledgeBase("D-ENGINE", this.kconf);

        this.ksession = kbase.newStatefulKnowledgeSession();
    }

    private void initDeployRule() throws RuleIllegalityException, EngineException, DbException {
        List<CorrelationRule> rules = ruleMgtWrapper.queryRuleByEnable(ENABLE);

        if (rules.size() > 0) {
            for (CorrelationRule rule : rules) {
                if (rule.getContent() != null) {
                    deployRuleFromCache(rule.getContent());
                }
            }
        }
    }

    private void deployRuleFromCache(String ruleContent) throws EngineException {
        StringReader reader = new StringReader(ruleContent);
        Resource res = ResourceFactory.newReaderResource(reader);

        if (kbuilder == null) {
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        }

        kbuilder.add(res, ResourceType.DRL);

        try {

            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (Exception e) {
            throw new EngineException(e);
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

    class AlarmMqMessageListener implements Runnable {

        public void run() {
            Connection connection;
            Session session;
            Destination destination;
            MessageConsumer messageConsumer;

            try {
                connection = connectionFactory.createConnection();
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                destination = session.createTopic(AlarmConst.MQ_TOPIC_NAME_ALARM);
                messageConsumer = session.createConsumer(destination);

                while (true) {
                    ObjectMessage objMessage = (ObjectMessage) messageConsumer.receive(100000);
                    if (objMessage != null) {
                        putRaisedIntoStream((Alarm) objMessage.getObject());
                    } else {
                        break;
                    }
                }
            } catch (JMSException e) {
                log.error("connection mq service Failed: " + e.getMessage());
            }

        }
    }

}
