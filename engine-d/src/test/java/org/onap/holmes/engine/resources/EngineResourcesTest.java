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

package org.onap.holmes.engine.resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.engine.manager.DroolsEngine;
import org.onap.holmes.engine.request.CompileRuleRequest;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import java.util.Locale;

import static org.easymock.EasyMock.*;

public class EngineResourcesTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    DroolsEngine droolsEngine;
    private EngineResources engineResources;

    @Before
    public void setUp() {
        droolsEngine = PowerMock.createMock(DroolsEngine.class);
        engineResources = new EngineResources();

        Whitebox.setInternalState(engineResources,"droolsEngine", droolsEngine);
        PowerMock.resetAll();
    }

    @Test
    public void deployRule_exception() throws CorrelationException {
        DeployRuleRequest deployRuleRequest = new DeployRuleRequest();
        HttpServletRequest httpRequest = PowerMock.createMock(HttpServletRequest.class);

        thrown.expect(WebApplicationException.class);

        expect(httpRequest.getHeader("language-option")).andReturn("en_US");
        expect(droolsEngine.deployRule(anyObject(DeployRuleRequest.class))).
                andThrow(new CorrelationException(""));
        PowerMock.replayAll();
        engineResources.deployRule(deployRuleRequest, httpRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void deployRule_normal() throws CorrelationException {
        DeployRuleRequest deployRuleRequest = new DeployRuleRequest();
        deployRuleRequest.setContent("package packageName;\n\nimport xxx.xxx.xxx;");
        deployRuleRequest.setLoopControlName("loopControlName");
        HttpServletRequest httpRequest = PowerMock.createMock(HttpServletRequest.class);

        expect(httpRequest.getHeader("language-option")).andReturn("en_US");
        expect(droolsEngine.deployRule(anyObject(DeployRuleRequest.class))).andReturn("packageName");
        PowerMock.replayAll();
        engineResources.deployRule(deployRuleRequest, httpRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void undeployRule_exception() throws CorrelationException {
        String packageName = "packageName";
        HttpServletRequest httpRequest = PowerMock.createMock(HttpServletRequest.class);

        thrown.expect(WebApplicationException.class);

        expect(httpRequest.getHeader("language-option")).andReturn("en_US");
        droolsEngine.undeployRule(anyObject(String.class));
        expectLastCall().andThrow(new CorrelationException(""));
        PowerMock.replayAll();
        engineResources.undeployRule(packageName, httpRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void undeployRule_normal() throws CorrelationException {
        String packageName = "packageName";
        HttpServletRequest httpRequest = PowerMock.createMock(HttpServletRequest.class);

        expect(httpRequest.getHeader("language-option")).andReturn("en_US");
        droolsEngine.undeployRule(anyObject(String.class));
        PowerMock.replayAll();
        engineResources.undeployRule(packageName, httpRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void compileRule_exception() throws CorrelationException {
        CompileRuleRequest compileRuleRequest = new CompileRuleRequest();
        HttpServletRequest httpRequest = PowerMock.createMock(HttpServletRequest.class);

        thrown.expect(WebApplicationException.class);

        expect(httpRequest.getHeader("language-option")).andReturn("en_US");
        droolsEngine.compileRule(anyObject(String.class));
        expectLastCall().andThrow(new CorrelationException(""));
        PowerMock.replayAll();
        engineResources.compileRule(compileRuleRequest, httpRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void compileRule_normal() throws CorrelationException {
        CompileRuleRequest compileRuleRequest = new CompileRuleRequest();
        HttpServletRequest httpRequest = PowerMock.createMock(HttpServletRequest.class);

        expect(httpRequest.getHeader("language-option")).andReturn("en_US");
        droolsEngine.compileRule(anyObject(String.class));
        PowerMock.replayAll();
        engineResources.compileRule(compileRuleRequest, httpRequest);
        PowerMock.verifyAll();
    }
}
