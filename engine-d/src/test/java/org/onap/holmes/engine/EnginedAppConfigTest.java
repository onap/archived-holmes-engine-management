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

package org.onap.holmes.engine;

import io.dropwizard.db.DataSourceFactory;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.holmes.common.config.MQConfig;
import org.powermock.api.easymock.PowerMock;

public class EnginedAppConfigTest {

    private EngineDAppConfig engineAppConfig;

    @Before
    public void setUp() {
        engineAppConfig = new EngineDAppConfig();
    }

    @Test
    public void getDataSourceFactory() {
        Assert.assertThat(engineAppConfig.getDataSourceFactory(), IsNull.<DataSourceFactory>notNullValue());
    }

    @Test
    public void setDataSourceFactory() {
        DataSourceFactory database = new DataSourceFactory();
        engineAppConfig.setDataSourceFactory(database);
        Assert.assertThat(engineAppConfig.getDataSourceFactory(), IsEqual.equalTo(database));
    }

    @Test
    public void getApidescription() {
        final String apidescription = "Holmes rule management rest API";
        Assert.assertThat(engineAppConfig.getApidescription(), IsEqual.equalTo(apidescription));
    }

    @Test
    public void setApidescription() {
        final String apidescription = "set api description";
        engineAppConfig.setApidescription(apidescription);
        Assert.assertThat(engineAppConfig.getApidescription(), IsEqual.equalTo(apidescription));
    }
}
