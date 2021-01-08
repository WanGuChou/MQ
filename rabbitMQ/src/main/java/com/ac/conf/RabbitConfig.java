package com.ac.conf;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * class description<br/>
 *
 * @author wangguocheng
 * @version 1.0
 * @date 2020/12/30 20:56
 * @since JDK 1.8+
 */
public interface RabbitConfig {


    String host = "localhost";
    int port = 5672;
    String exchange = "exchange_demo";
    String routing_key = "routing_key_demo";
    String queue = "queue_demo";
    String username = "guest";
    String password = "guest";

    ConnectionFactory factory = build();


    static ConnectionFactory build() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitConfig.host);
        factory.setPort(RabbitConfig.port);
        factory.setUsername(RabbitConfig.username);
        factory.setPassword(RabbitConfig.password);
        return factory;
    }

    static Connection newConnection() {
        try {
            return factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();

        }
        return null;
    }


    static Channel newChannel() {
        try {
            return newConnection().createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
