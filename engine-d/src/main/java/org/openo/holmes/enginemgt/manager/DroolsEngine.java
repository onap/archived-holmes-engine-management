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
package org.openo.holmes.enginemgt.manager;


import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.common.api.stat.Alarm;
import org.openo.holmes.common.exception.DbException;
import org.openo.holmes.common.exception.EngineException;
import org.openo.holmes.common.exception.RuleIllegalityException;
import org.openo.holmes.common.utils.ExceptionUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.enginemgt.listener.AlarmMqMessageListener;
import org.openo.holmes.enginemgt.request.DeployRuleRequest;
import org.openo.holmes.enginemgt.wrapper.RuleMgtWrapper;

@Slf4j
@Service
public class DroolsEngine {

    private final static String CORRELATION_RULE = "CORRELATION_RULE";
    private final static String CORRELATION_ALARM = "CORRELATION_ALARM";
    private final static int ENABLE = 1;
    @Inject
    private RuleMgtWrapper ruleMgtWrapper;
    @Inject
    private AlarmMqMessageListener mqRegister;
    private KnowledgeBase kbase;
    private KnowledgeBaseConfiguration kconf;
    private StatefulKnowledgeSession ksession;
    private KnowledgeBuilder kbuilder;

    @PostConstruct
    private void init() {
        registerAlarmTopicListener();
        try {
            start();
        } catch (Exception e) {
            log.error("Start service failed: " + e.getMessage());
            throw ExceptionUtil.buildExceptionResponse("Start service failed!");
        }
    }

    private void registerAlarmTopicListener() {
        Thread thread = new Thread(mqRegister);
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

        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(res, ResourceType.DRL);

        try {

            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (Exception e) {
            throw new EngineException(e);
        }

        kbuilder = null;

        ksession.fireAllRules();
    }

    public synchronized String deployRule(DeployRuleRequest rule, Locale locale)
        throws RuleIllegalityException, EngineException {
        StringReader reader = new StringReader(rule.getContent());
        Resource res = ResourceFactory.newReaderResource(reader);

        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(res, ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            throw new RuleIllegalityException(kbuilder.getErrors().toString());
        }

        String packageName = kbuilder.getKnowledgePackages().iterator().next().getName();

        if (kbase.getKnowledgePackages().contains(packageName)) {
            throw new RuleIllegalityException(
                I18nProxy.getInstance().getValue(locale, I18nProxy.ENGINE_CONTAINS_PACKAGE));
        }
        try {

            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (Exception e) {
            throw new EngineException(e);
        }

        kbuilder = null;

        ksession.fireAllRules();
        return packageName;
    }


    public synchronized void undeployRule(String packageName)
        throws RuleIllegalityException, EngineException {

        KnowledgePackage pkg = kbase.getKnowledgePackage(packageName);

        if (null == pkg) {
            throw new RuleIllegalityException(packageName);
        }

        try {

            kbase.removeKnowledgePackage(pkg.getName());
        } catch (Exception e) {

            throw new EngineException(e);
        }
    }

    public void compileRule(String content, Locale locale)
        throws RuleIllegalityException {
        StringReader reader = new StringReader(content);
        Resource res = ResourceFactory.newReaderResource(reader);

        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(res, ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            throw new RuleIllegalityException(kbuilder.getErrors().toString());
        }
        kbuilder = null;
    }

    public void putRaisedIntoStream(Alarm raiseAlarm) {
        FactHandle factHandle = this.ksession.getFactHandle(raiseAlarm);
        if (factHandle != null) {
            this.ksession.retract(factHandle);
        }
        this.ksession.insert(raiseAlarm);
        this.ksession.fireAllRules();
    }
}
