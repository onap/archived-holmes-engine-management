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


import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.dmaap.store.ClosedLoopControlNameCache;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.ExceptionUtil;
import org.onap.holmes.common.utils.LanguageUtil;
import org.onap.holmes.engine.manager.DroolsEngine;
import org.onap.holmes.engine.request.CompileRuleRequest;
import org.onap.holmes.engine.request.DeployRuleRequest;
import org.onap.holmes.engine.response.CorrelationRuleResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Path("/rule")
@Api(tags = {"Holmes Engine Management"})
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class EngineResources {
    @Inject
    DroolsEngine droolsEngine;
	private Pattern packagePattern = Pattern.compile("package[\\s]+([^;]+)[;\\s]*");
    private ClosedLoopControlNameCache closedLoopControlNameCache;

    @Inject
    public void setClosedLoopControlNameCache(ClosedLoopControlNameCache closedLoopControlNameCache) {
        this.closedLoopControlNameCache = closedLoopControlNameCache;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public CorrelationRuleResponse deployRule(
            @ApiParam(value = "The request entity of the HTTP call, which comprises three "
                    + "fields: \"content\" , \"loopControlName\" and \"engineId\". "
                    + "The \"content\" should be a valid Drools rule string and the \"engineId\" "
                    + "has to be \"engine-d\" in the Amsterdam release.", required = true) DeployRuleRequest deployRuleRequest,
            @Context HttpServletRequest httpRequest) {

        CorrelationRuleResponse crResponse = new CorrelationRuleResponse();
        Locale locale = LanguageUtil.getLocale(httpRequest);
        try {
            String packageName = getPackageName(deployRuleRequest.getContent());
            if(packageName == null) {
            	throw new CorrelationException("Could not find package name in rule: "+deployRuleRequest.getContent());
            }
            
            closedLoopControlNameCache
                    .put(packageName, deployRuleRequest.getLoopControlName());
            String packageNameRet = droolsEngine.deployRule(deployRuleRequest);
            if (!packageName.equals(packageNameRet)) {
                log.info("The parsed package name is different from that returned by the engine.");
                closedLoopControlNameCache.remove(packageName);
                closedLoopControlNameCache
                        .put(packageNameRet, deployRuleRequest.getLoopControlName());
            }
            log.info("Rule deployed. Package name: " + packageNameRet);
            crResponse.setPackageName(packageNameRet);

        } catch (CorrelationException correlationException) {
            log.error(correlationException.getMessage(), correlationException);
            throw ExceptionUtil.buildExceptionResponse(correlationException.getMessage());
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }

        return crResponse;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Path("/{packageName}")
    public boolean undeployRule(@PathParam("packageName") String packageName,
            @Context HttpServletRequest httpRequest) {

        Locale locale = LanguageUtil.getLocale(httpRequest);

        try {
            droolsEngine.undeployRule(packageName);
            closedLoopControlNameCache.remove(packageName);
        } catch (CorrelationException correlationException) {
            log.error(correlationException.getMessage(), correlationException);
            throw ExceptionUtil.buildExceptionResponse(correlationException.getMessage());
        }

        return true;
    }


    @POST
    @ApiOperation(value = "Check the validity of a rule.")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public boolean compileRule(CompileRuleRequest compileRuleRequest,
            @Context HttpServletRequest httpRequest) {

        Locale locale = LanguageUtil.getLocale(httpRequest);

        try {
            droolsEngine.compileRule(compileRuleRequest.getContent());
        } catch (CorrelationException correlationException) {
            log.error(correlationException.getMessage(), correlationException);
            throw ExceptionUtil.buildExceptionResponse(correlationException.getMessage());
        }
        return true;
    }
    
    private String getPackageName(String contents){
        Matcher m = packagePattern.matcher(contents);
        
        if (m.find( )) {
           return m.group(1);
        }else {
           return null;
        }
    }
}
