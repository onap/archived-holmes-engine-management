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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.dmaap.DmaapService;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.common.api.entity.CorrelationRule;
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

    private KieBase kieBase;
    private KieSession kieSession;
    private KieContainer kieContainer;
    private KieFileSystem kfs;
    private KieServices ks;
    private KieBuilder kieBuilder;
    private KieResources resources;
    private KieRepository kieRepository;

    @PostConstruct
    private void init() {
        try {
            // start engine
            start();
        } catch (Exception e) {
            log.error("Failed to start the service: " + e.getMessage(), e);
            throw ExceptionUtil.buildExceptionResponse("Failed to start the drools engine!");
        }
    }

    private void start() throws CorrelationException {
        log.info("Drools Engine Initialize Beginning...");

        initEngineParameter();
        initDeployRule();

        log.info("Business Rule Engine Initialize Successfully.");
    }

    public void stop() {
        this.kieSession.dispose();
    }

    public void initEngineParameter() {
        this.ks = KieServices.Factory.get();
        this.resources = ks.getResources();
        this.kieRepository = ks.getRepository();
        this.kfs = createKieFileSystemWithKProject(ks);

        this.kieBuilder = ks.newKieBuilder(kfs).buildAll();
        this.kieContainer = ks.newKieContainer(kieRepository.getDefaultReleaseId());

        this.kieBase = kieContainer.getKieBase();
        this.kieSession = kieContainer.newKieSession();
    }

    private void initDeployRule() throws CorrelationException {
        List<CorrelationRule> rules = ruleMgtWrapper.queryRuleByEnable(ENABLE);

        if (rules.isEmpty()) {
            return;
        }
        for (CorrelationRule rule : rules) {
            if (rule.getContent() != null) {
                deployRuleFromDB(rule.getContent());
                DmaapService.loopControlNames.put(rule.getPackageName(), rule.getClosedControlLoopName());
            }
        }
    }

    private void deployRuleFromDB(String ruleContent) throws CorrelationException {
        avoidDeployBug();
        StringReader reader = new StringReader(ruleContent);
        kfs.write("src/main/resources/rules/rule.drl",
                this.resources.newReaderResource(reader,"UTF-8").setResourceType(ResourceType.DRL));
        kieBuilder = ks.newKieBuilder(kfs).buildAll();
        try {
            InternalKieModule internalKieModule = (InternalKieModule)kieBuilder.getKieModule();
            kieContainer.updateToVersion(internalKieModule.getReleaseId());
        } catch (Exception e) {
            throw new CorrelationException(e.getMessage(), e);
        }
        kieSession.fireAllRules();
    }

    public synchronized String deployRule(DeployRuleRequest rule, Locale locale)
        throws CorrelationException {
        avoidDeployBug();
        StringReader reader = new StringReader(rule.getContent());
        kfs.write("src/main/resources/rules/rule.drl",
                this.resources.newReaderResource(reader,"UTF-8").setResourceType(ResourceType.DRL));
        kieBuilder = ks.newKieBuilder(kfs).buildAll();

        judgeRuleContent(locale, kieBuilder, true);

        InternalKieModule internalKieModule = (InternalKieModule)kieBuilder.getKieModule();;
        String packageName = internalKieModule.getKnowledgePackagesForKieBase("KBase").iterator().next().getName();
        try {
            kieContainer.updateToVersion(internalKieModule.getReleaseId());
        } catch (Exception e) {
            throw new CorrelationException("Failed to deploy the rule.", e);
        }
        packageNames.add(packageName);
        kieSession.fireAllRules();
        return packageName;
    }

    public synchronized void undeployRule(String packageName, Locale locale)
        throws CorrelationException {
        KiePackage kiePackage = kieBase.getKiePackage(packageName);
        if (null == kiePackage) {
            throw new CorrelationException("The rule " + packageName + " does not exist!");
        }
        try {
            kieBase.removeKiePackage(kiePackage.getName());
        } catch (Exception e) {
            throw new CorrelationException("Failed to delete the rule: " + packageName, e);
        }
        packageNames.remove(kiePackage.getName());
    }

    public void compileRule(String content, Locale locale)
        throws CorrelationException {
        StringReader reader = new StringReader(content);

        kfs.write("src/main/resources/rules/rule.drl",
                this.resources.newReaderResource(reader,"UTF-8").setResourceType(ResourceType.DRL));

        kieBuilder = ks.newKieBuilder(kfs).buildAll();

        judgeRuleContent(locale, kieBuilder, false);
    }

    private void judgeRuleContent(Locale locale, KieBuilder kbuilder, boolean judgePackageName)
        throws CorrelationException {
        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            String errorMsg = "There are errors in the rule: " + kbuilder.getResults()
                    .getMessages(Level.ERROR).toString();
            log.error(errorMsg);
            throw new CorrelationException(errorMsg);
        }
        InternalKieModule internalKieModule = null;
        try {
            internalKieModule = (InternalKieModule) kbuilder.getKieModule();
        } catch (Exception e) {
            throw new CorrelationException("There are errors in the rule!" + e.getMessage(), e);
        }
        if (internalKieModule == null) {
            throw new CorrelationException("There are errors in the rule!");
        }
        String packageName = internalKieModule.getKnowledgePackagesForKieBase("KBase").iterator().next().getName();

        if (queryAllPackage().contains(packageName) && judgePackageName) {
            throw new CorrelationException("The rule " + packageName + " already exists in the drools engine.");
        }
    }

    public void putRaisedIntoStream(VesAlarm raiseAlarm) {
        FactHandle factHandle = this.kieSession.getFactHandle(raiseAlarm);
        if (factHandle != null) {
            Object obj = this.kieSession.getObject(factHandle);
            if (obj != null && obj instanceof VesAlarm) {
                raiseAlarm.setRootFlag(((VesAlarm) obj).getRootFlag());
            }
            this.kieSession.delete(factHandle);
        }
        this.kieSession.insert(raiseAlarm);
        this.kieSession.fireAllRules();
    }

    public List<String> queryAllPackage() {
        List<KiePackage> kiePackages = (List<KiePackage>)kieBase.getKiePackages();
        List<String> list = new ArrayList<>();
        for(KiePackage kiePackage : kiePackages) {
            list.add(kiePackage.getName());
        }
        return list;
    }

    private KieFileSystem createKieFileSystemWithKProject(KieServices ks) {
        KieModuleModel kieModuleModel = ks.newKieModuleModel();
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("KBase")
                .addPackage("rules")
                .setDefault(true)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM);
        KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel("KSession")
                .setDefault( true )
                .setType( KieSessionModel.KieSessionType.STATEFUL )
                .setClockType( ClockTypeOption.get("realtime") );
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kieModuleModel.toXML());
        return kfs;
    }

    private void avoidDeployBug() {
        String tmp = Math.random() + "";
        String rule = "package justInOrderToAvoidDeployBug" + tmp.substring(2);
        kfs.write("src/main/resources/rules/rule.drl", rule);
        kieBuilder = ks.newKieBuilder(kfs).buildAll();
        InternalKieModule internalKieModule = (InternalKieModule)kieBuilder.getKieModule();
        String packageName = internalKieModule.getKnowledgePackagesForKieBase("KBase").iterator().next().getName();
        kieRepository.addKieModule(internalKieModule);
        kieContainer.updateToVersion(internalKieModule.getReleaseId());

        KiePackage kiePackage = kieBase.getKiePackage(packageName);
        kieBase.removeKiePackage(kiePackage.getName());
    }

}
