/*
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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.dcae.DcaeConfigurationsCache;
import org.onap.holmes.common.dcae.entity.SecurityInfo;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.dsa.dmaappolling.Subscriber;
import org.onap.holmes.engine.dmaap.SubscriberAction;
import org.onap.holmes.engine.request.DmaapConfigRequest;

@Service
@Slf4j
//@Api(tags = {"DMaaP Configurations"})
@Path("/dmaap")
public class DmaapConfigurationService {
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Subscribe to a new topic. "
            + "If the topic already exists, it is replaced with the new configuration.")
    @Path("/sub")
    public String addSubInfo(
            @ApiParam (value = "A JSON object with the fields named <b>name</b>"
                    + " and <b>url</b>. Both fields are required.") DmaapConfigRequest config,
            @Context HttpServletRequest request){
        String url = config.getUrl();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Subscriber subscriber = new Subscriber();
            subscriber.setTopic(config.getName());
            subscriber.setUrl(url);

            SubscriberAction subscriberAction = ServiceLocatorHolder.getLocator()
                    .getService(SubscriberAction.class);
            subscriberAction.removeSubscriber(subscriber);
            subscriberAction.addSubscriber(subscriber);

            log.info("New configurations applied. Topic Name: " + config.getName() + ", URL: " + url + ".");

            return "{\"message\": \"Succeeded!\", \"topic\": \"" + config.getName() + "\"}";
        }
        return "{\"message\": \"Only the HTTP or HTTPS protocol is supported!\"}";
    }

    @DELETE
    @Path("/sub/{topic}")
    @ApiOperation(value = "Unsubscribe a topic from DMaaP.")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeSubInfo(@PathParam("topic") String topic){
        Subscriber subscriber = new Subscriber();
        subscriber.setTopic(topic);

        SubscriberAction subscriberAction = ServiceLocatorHolder.getLocator()
                .getService(SubscriberAction.class);
        subscriberAction.removeSubscriber(subscriber);

        return "{\"message\": \"Topic unsubscribed.\"}";
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pub")
    @ApiOperation(value = "Add/Update a publishing topic. "
            + "If the topic already exists, it is replaced with the new configuration.")
    public String updatePubInfo(
            @ApiParam (value = "A JSON object with the fields named <b>name</b>"
                + " and <b>url</b>. Both fields are required.") DmaapConfigRequest config,
            @Context HttpServletRequest request){
        String url = config.getUrl();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            SecurityInfo securityInfo = new SecurityInfo();
            SecurityInfo.DmaapInfo dmaapInfo = new SecurityInfo().new DmaapInfo();
            dmaapInfo.setTopicUrl(config.getUrl());
            securityInfo.setDmaapInfo(dmaapInfo);
            DcaeConfigurationsCache.addPubSecInfo(config.getName(), securityInfo);
            return "{\"message\": \"Succeeded!\", \"topic\": \"" + config.getName() + "\"}";
        }
        return "{\"message\": \"Only the HTTP or HTTPS protocol is supported!\"}";
    }
}
