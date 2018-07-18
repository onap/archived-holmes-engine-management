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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;

@Service
@Path("/swagger.json")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class SwaggerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getSwaggerJson() {
        URL url = SwaggerResource.class.getResource("/swagger.json");
        String ret = "{}";
        File file = null;

        try {
            System.out.println(URLDecoder.decode(url.getPath(), "UTF-8"));
            file = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
        } catch (IOException e) {
            log.warn("An error occurred while decoding url");
        }

        if (file == null) {
            log.warn("Unable to get Swagger Json since API description file could not be read");
            return ret;
        }

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuffer buffer = new StringBuffer();
            String line = " ";
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            ret = buffer.toString();
        } catch (IOException e) {
            log.warn("An error occurred while reading swagger.json.");
        }
        return ret;
    }
}
