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

import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.util.StringUtils;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.onap.holmes.common.api.entity.AlarmInfo;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.dmaap.DmaapService;
import org.onap.holmes.common.exception.AlarmInfoException;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.common.utils.ExceptionUtil;
import org.onap.holmes.engine.db.AlarmInfoDao;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.engine.wrapper.RuleMgtWrapper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DroolsEngine {

    @Inject
    private RuleMgtWrapper ruleMgtWrapper;
    @Inject
    private DbDaoUtil daoUtil;

    private final static int ENABLE = 1;
    private AlarmInfoDao alarmInfoDao;
    private final Map<String, String> deployed = new ConcurrentHashMap<>();
    private KieServices ks = KieServices.Factory.get();
    private ReleaseId releaseId = ks.newReleaseId("org.onap.holmes", "rules", "1.0.0-SNAPSHOT");
    private ReleaseId compilationRelease = ks.newReleaseId("org.onap.holmes", "compilation", "1.0.0-SNAPSHOT");
    private KieContainer container;
    private KieSession session;

    @PostConstruct
    private void init() {
        alarmInfoDao = daoUtil.getJdbiDaoByOnDemand(AlarmInfoDao.class);
        try {
            log.info("Drools engine initializing...");
            initEngine();
            log.info("Drools engine initialized.");

            log.info("Start deploy existing rules...");
            initRules();
            log.info("All rules were deployed.");

            log.info("Synchronizing alarms...");
            syncAlarms();
            log.info("Alarm synchronization succeeded.");
        } catch (Exception e) {
            log.error("Failed to startup the engine of Holmes: " + e.getMessage(), e);
            throw ExceptionUtil.buildExceptionResponse("Failed to startup Drools!");
        }
    }

    public void stop() {
        session.dispose();
    }

    public void initEngine() {
        KieModule km = null;
        try {
            String drl = "package holmes;";
            deployed.put(getPackageName(drl), drl);
            km = createAndDeployJar(ks, releaseId, new ArrayList<>(deployed.values()));
        } catch (Exception e) {
            log.error("Failed to initialize the engine service module.", e);
        }
        if (null != km) {
            container = ks.newKieContainer(km.getReleaseId());
        }
        session = container.newKieSession();
        deployed.clear();
    }

    private void initRules() throws CorrelationException {
        List<CorrelationRule> rules = ruleMgtWrapper.queryRuleByEnable(ENABLE);
        if (rules.isEmpty()) {
            return;
        }

        for (CorrelationRule rule : rules) {
            if (!StringUtils.isEmpty(rule.getContent())) {
                deployRule(rule.getContent());
                DmaapService.loopControlNames.put(rule.getPackageName(), rule.getClosedControlLoopName());
            }
        }

        session.fireAllRules();
    }

    public void syncAlarms() throws AlarmInfoException {
        alarmInfoDao.queryAllAlarm().forEach(alarmInfo -> putRaisedIntoStream(convertAlarmInfo2VesAlarm(alarmInfo)));
    }

    public String deployRule(DeployRuleRequest rule) throws CorrelationException {
        return deployRule(rule.getContent());
    }

    private synchronized String deployRule(String rule) throws CorrelationException {
        final String packageName = getPackageName(rule);

        if (StringUtils.isEmpty(packageName)) {
            throw new CorrelationException("The package name can not be empty.");
        }

        if (deployed.containsKey(packageName)) {
            throw new CorrelationException("A rule with the same package name already exists in the system.");
        }

        if (!StringUtils.isEmpty(rule)) {
            deployed.put(packageName, rule);
            try {
                refreshInMemRules();
            } catch (CorrelationException e) {
                deployed.remove(packageName);
                throw e;
            }
            session.fireAllRules();
        }

        return packageName;
    }

    public synchronized void undeployRule(String packageName) throws CorrelationException {

        if (StringUtils.isEmpty(packageName)) {
            throw new CorrelationException("The package name should not be null.");
        }

        if (!deployed.containsKey(packageName)) {
            throw new CorrelationException("The rule " + packageName + " does not exist!");
        }

        String removed = deployed.remove(packageName);
        try {
            refreshInMemRules();
        } catch (Exception e) {
            deployed.put(packageName, removed);
            throw new CorrelationException("Failed to delete the rule: " + packageName, e);
        }
    }

    private void refreshInMemRules() throws CorrelationException {
        KieModule km = createAndDeployJar(ks, releaseId, new ArrayList<>(deployed.values()));
        container.updateToVersion(km.getReleaseId());
    }

    public void compileRule(String content)
            throws CorrelationException {

        KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(compilationRelease);
        kfs.write("src/main/resources/rules/rule.drl", content);
        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();
        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            String errorMsg = "There are errors in the rule: " + builder.getResults()
                    .getMessages(Level.ERROR).toString();
            log.info("Compilation failure: " + errorMsg);
            throw new CorrelationException(errorMsg);
        }

        if (deployed.containsKey(getPackageName(content))) {
            throw new CorrelationException("There's no compilation error. But a rule with the same package name already " +
                    "exists in the engine, which may cause a deployment failure.");
        }

        ks.getRepository().removeKieModule(compilationRelease);
    }

    public void putRaisedIntoStream(VesAlarm alarm) {
        FactHandle factHandle = this.session.getFactHandle(alarm);
        if (factHandle != null) {
            Object obj = this.session.getObject(factHandle);
            if (obj != null && obj instanceof VesAlarm) {
                alarm.setRootFlag(((VesAlarm) obj).getRootFlag());
            }
            this.session.delete(factHandle);
        }

        this.session.insert(alarm);

        this.session.fireAllRules();
    }

    public List<String> queryPackagesFromEngine() {
        return container.getKieBase().getKiePackages().stream()
                .filter(pkg -> pkg.getRules().size() != 0)
                .map(pkg -> pkg.getName())
                .collect(Collectors.toList());
    }

    private VesAlarm convertAlarmInfo2VesAlarm(AlarmInfo alarmInfo) {
        VesAlarm vesAlarm = new VesAlarm();
        vesAlarm.setEventId(alarmInfo.getEventId());
        vesAlarm.setEventName(alarmInfo.getEventName());
        vesAlarm.setStartEpochMicrosec(alarmInfo.getStartEpochMicroSec());
        vesAlarm.setSourceId(alarmInfo.getSourceId());
        vesAlarm.setSourceName(alarmInfo.getSourceName());
        vesAlarm.setRootFlag(alarmInfo.getRootFlag());
        vesAlarm.setAlarmIsCleared(alarmInfo.getAlarmIsCleared());
        vesAlarm.setLastEpochMicrosec(alarmInfo.getLastEpochMicroSec());
        return vesAlarm;
    }

    private AlarmInfo convertVesAlarm2AlarmInfo(VesAlarm vesAlarm) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setEventId(vesAlarm.getEventId());
        alarmInfo.setEventName(vesAlarm.getEventName());
        alarmInfo.setStartEpochMicroSec(vesAlarm.getStartEpochMicrosec());
        alarmInfo.setLastEpochMicroSec(vesAlarm.getLastEpochMicrosec());
        alarmInfo.setSourceId(vesAlarm.getSourceId());
        alarmInfo.setSourceName(vesAlarm.getSourceName());
        alarmInfo.setAlarmIsCleared(vesAlarm.getAlarmIsCleared());
        alarmInfo.setRootFlag(vesAlarm.getRootFlag());

        return alarmInfo;
    }

    private String getPackageName(String contents) {
        String ret = contents.trim();
        StringBuilder stringBuilder = new StringBuilder();
        if (ret.startsWith("package")) {
            ret = ret.substring(7).trim();
            for (int i = 0; i < ret.length(); i++) {
                char tmp = ret.charAt(i);
                if (tmp == ';' || tmp == ' ' || tmp == '\n') {
                    break;
                }
                stringBuilder.append(tmp);
            }
        }
        return stringBuilder.toString();
    }

    private KieModule createAndDeployJar(KieServices ks, ReleaseId releaseId, List<String> drls) throws CorrelationException {
        byte[] jar = createJar(ks, releaseId, drls);
        KieModule km = deployJarIntoRepository(ks, jar);
        return km;
    }

    private byte[] createJar(KieServices ks, ReleaseId releaseId, List<String> drls) throws CorrelationException {
        KieModuleModel kieModuleModel = ks.newKieModuleModel();
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("KBase")
                .setDefault(true)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY);
        kieBaseModel.newKieSessionModel("KSession")
                .setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL);
        KieFileSystem kfs = ks.newKieFileSystem().writeKModuleXML(kieModuleModel.toXML()).generateAndWritePomXML(releaseId);

        int i = 0;
        for (String drl : drls) {
            if (!StringUtils.isEmpty(drl)) {
                kfs.write("src/main/resources/" + getPackageName(drl) + ".drl", drl);
            }
        }
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            StringBuilder sb = new StringBuilder();
            for (Message msg : kb.getResults().getMessages()) {
                sb.append(String.format("[%s]Line: %d, Col: %d\t%s\n", msg.getLevel().toString(), msg.getLine(),
                        msg.getColumn(), msg.getText()));
            }
            throw new CorrelationException("Failed to compile JAR. Details: \n" + sb.toString());
        }

        InternalKieModule kieModule = (InternalKieModule) ks.getRepository()
                .getKieModule(releaseId);

        return kieModule.getBytes();
    }

    private KieModule deployJarIntoRepository(KieServices ks, byte[] jar) {
        Resource jarRes = ks.getResources().newByteArrayResource(jar);
        return ks.getRepository().addKieModule(jarRes);
    }

}
