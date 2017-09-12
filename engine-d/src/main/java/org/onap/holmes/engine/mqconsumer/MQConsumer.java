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
package org.onap.holmes.engine.mqconsumer;

import java.io.Serializable;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.stat.VesAlarm;
import org.onap.holmes.common.config.MQConfig;
import org.onap.holmes.common.constant.AlarmConst;
import org.onap.holmes.engine.manager.DroolsEngine;

@Service
@Slf4j
@NoArgsConstructor
public class MQConsumer {

    @Inject
    private IterableProvider<MQConfig> mqConfigProvider;
    private ConnectionFactory connectionFactory;
    private ConnectionFactory connectionFactory1;
    @Inject
    private DroolsEngine engine;

    public void registerAlarmTopicListener() {
        String brokerURL =
                "tcp://" + mqConfigProvider.get().brokerIp + ":" + mqConfigProvider.get().brokerPort;
        connectionFactory = new ActiveMQConnectionFactory(mqConfigProvider.get().brokerUsername,
                mqConfigProvider.get().brokerPassword, brokerURL);

        AlarmMqMessageListener listener = new AlarmMqMessageListener();
        listener.receive();
    }
    class AlarmMqMessageListener implements MessageListener {

        private Connection connection = null;
        private Session session = null;
        private Destination destination = null;
        private MessageConsumer consumer = null;

        private void initialize() throws JMSException {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createTopic(AlarmConst.MQ_TOPIC_NAME_ALARM);
            consumer = session.createConsumer(destination);
            connection.start();
        }

        public void receive() {
            try {
                initialize();
                consumer.setMessageListener(this);
            } catch (JMSException e) {
                log.error("Failed to connect to the MQ service : " + e.getMessage(), e);
                try {
                    close();
                } catch (JMSException e1) {
                    log.error("Failed close connection  " + e1.getMessage(), e1);
                }
            }
        }

        public void onMessage(Message arg0) {
            ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) arg0;
            try {
                Serializable object = objectMessage.getObject();
                if (object instanceof VesAlarm) {
                    VesAlarm vesAlarm = (VesAlarm) object;
                    engine.putRaisedIntoStream(vesAlarm);
                }
            } catch (JMSException e) {
                log.error("Failed get object : " + e.getMessage(), e);
            }
        }

        private void close() throws JMSException {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
