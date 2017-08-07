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
package org.onap.holmes.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.config.MQConfig;

@Service
public class EngineDAppConfig extends Configuration {

    @NotEmpty
    private String defaultName = "Correlation-Rule";

    @NotEmpty
    private String apidescription = "Holmes rule management rest API";

    @JsonProperty
    @NotNull
    @Valid
    private MQConfig mqConfig;
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    public MQConfig getMqConfig() {
        return mqConfig;
    }

    public void setMqConfig(MQConfig mqConfig) {
        this.mqConfig = mqConfig;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    public String getApidescription() {
        return apidescription;
    }

    public void setApidescription(String apidescription) {
        this.apidescription = apidescription;
    }
}
