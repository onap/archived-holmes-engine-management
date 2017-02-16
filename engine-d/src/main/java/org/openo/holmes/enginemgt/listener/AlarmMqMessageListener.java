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
package org.openo.holmes.enginemgt.listener;


import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.stat.Alarm;
import org.openo.holmes.common.config.MQConfig;
import org.openo.holmes.common.constant.AlarmConst;
import org.openo.holmes.enginemgt.manager.DroolsEngine;

@Service
@Slf4j
public class AlarmMqMessageListener implements Runnable {

    @Inject
    private static IterableProvider<MQConfig> mqConfigProvider;
    @Inject
    DroolsEngine droolsEngine;
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() {

        String brokerURL =
            "tcp://" + mqConfigProvider.get().brokerIp + ":" + mqConfigProvider.get().brokerPort;
        connectionFactory = new ActiveMQConnectionFactory(mqConfigProvider.get().brokerUsername,
            mqConfigProvider.get().brokerPassword, brokerURL);
    }


    public void run() {
        Connection connection;
        Session session;
        Destination destination;
        MessageConsumer messageConsumer;

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createTopic(AlarmConst.MQ_TOPIC_NAME_ALARM);
            messageConsumer = session.createConsumer(destination);

            while (true) {
                ObjectMessage objMessage = (ObjectMessage) messageConsumer.receive(100000);
                if (objMessage != null) {
                    droolsEngine.putRaisedIntoStream((Alarm) objMessage.getObject());
                } else {
                    break;
                }
            }
        } catch (JMSException e) {
            log.debug("Receive alarm failure" + e.getMessage());
        }

    }
}
