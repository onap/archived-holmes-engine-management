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

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.easymock.EasyMock;
import org.glassfish.hk2.api.IterableProvider;
import org.junit.Before;
import org.junit.Test;
import org.onap.holmes.common.api.stat.Alarm;
import org.onap.holmes.common.config.MQConfig;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class MQConsumerTest {

    private IterableProvider<MQConfig> mqConfigProvider;

    private ConnectionFactory connectionFactory;

    private MQConsumer mqConsumer;

    @Before
    public void setUp() {

        mqConsumer = new MQConsumer();

        mqConfigProvider = PowerMock.createMock(IterableProvider.class);
        connectionFactory = PowerMock.createMock(ConnectionFactory.class);

        Whitebox.setInternalState(mqConsumer, "mqConfigProvider", mqConfigProvider);
        Whitebox.setInternalState(mqConsumer, "connectionFactory", connectionFactory);
    }

    @Test
    public void init() throws Exception {
        MQConfig mqConfig = new MQConfig();
        mqConfig.brokerIp = "127.0.0.1";
        mqConfig.brokerPort = 4567;
        mqConfig.brokerUsername = "admin";
        mqConfig.brokerPassword = "admin";

        expect(mqConfigProvider.get()).andReturn(mqConfig).anyTimes();
        PowerMock.replayAll();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_receive() throws JMSException {
        MQConsumer.AlarmMqMessageListener listener = mqConsumer.new AlarmMqMessageListener();

        Connection connection = PowerMock.createMock(Connection.class);
        Session session = PowerMock.createMock(Session.class);
        Destination destination = PowerMock.createMock(Topic.class);
        MessageConsumer consumer = PowerMock.createMock(MessageConsumer.class);

        Whitebox.setInternalState(listener, "connection", connection);
        Whitebox.setInternalState(listener, "session", session);
        Whitebox.setInternalState(listener, "destination", destination);
        Whitebox.setInternalState(listener, "consumer", consumer);

        PowerMock.reset();

        expect(connectionFactory.createConnection()).andReturn(connection);
        connection.start();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session);
        expect(session.createTopic(anyObject(String.class))).andReturn((Topic) destination);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(consumer);
        consumer.setMessageListener(listener);

        PowerMock.replayAll();

        listener.receive();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_exception() throws JMSException {
        MQConsumer.AlarmMqMessageListener listener = mqConsumer.new AlarmMqMessageListener();

        Connection connection = PowerMock.createMock(Connection.class);
        Session session = PowerMock.createMock(Session.class);
        Destination destination = PowerMock.createMock(Topic.class);
        MessageConsumer consumer = PowerMock.createMock(MessageConsumer.class);

        Whitebox.setInternalState(listener, "connection", connection);
        Whitebox.setInternalState(listener, "session", session);
        Whitebox.setInternalState(listener, "destination", destination);
        Whitebox.setInternalState(listener, "consumer", consumer);

        PowerMock.reset();

        expect(connectionFactory.createConnection()).andReturn(connection);
        connection.start();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session);
        expect(session.createTopic(anyObject(String.class))).andReturn((Topic) destination);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(consumer);
        consumer.setMessageListener(listener);
        EasyMock.expectLastCall().andThrow(new JMSException(""));

        consumer.close();
        session.close();
        connection.close();

        PowerMock.replayAll();

        listener.receive();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_close_exception() throws JMSException {
        MQConsumer.AlarmMqMessageListener listener = mqConsumer.new AlarmMqMessageListener();

        Connection connection = PowerMock.createMock(Connection.class);
        Session session = PowerMock.createMock(Session.class);
        Destination destination = PowerMock.createMock(Topic.class);
        MessageConsumer consumer = PowerMock.createMock(MessageConsumer.class);

        Whitebox.setInternalState(listener, "connection", connection);
        Whitebox.setInternalState(listener, "session", session);
        Whitebox.setInternalState(listener, "destination", destination);
        Whitebox.setInternalState(listener, "consumer", consumer);

        PowerMock.reset();

        expect(connectionFactory.createConnection()).andReturn(connection);
        connection.start();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session);
        expect(session.createTopic(anyObject(String.class))).andReturn((Topic) destination);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(consumer);
        consumer.setMessageListener(listener);
        EasyMock.expectLastCall().andThrow(new JMSException(""));

        consumer.close();
        EasyMock.expectLastCall().andThrow(new JMSException(""));

        PowerMock.replayAll();

        listener.receive();

        PowerMock.verifyAll();
    }

    @Test
    public void listener_on_message() throws JMSException {
        MQConsumer.AlarmMqMessageListener listener = mqConsumer.new AlarmMqMessageListener();
        Alarm alarm = new Alarm();
        alarm.setAlarmKey("alarmKey");
        ActiveMQObjectMessage objectMessage = new ActiveMQObjectMessage();
        objectMessage.setObject(alarm);

        listener.onMessage(objectMessage);
    }
}
