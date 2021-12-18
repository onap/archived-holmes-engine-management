/*
 * Copyright 2017-2021 ZTE Corporation.
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

@Slf4j
@RestController
@RequestMapping("/swagger.json")
public class SwaggerResource {

    @GetMapping(produces = MediaType.APPLICATION_JSON)
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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
