package com.ac.c3.demo2exchange;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * class description<br/>
 *
 * @author wangguocheng
 * @version 1.0
 * @date 2020/12/30 20:54
 * @since JDK 1.8+
 */
public class RabbitConsumer {


    public static void main(String[] args) throws Exception {


        Channel channel = RabbitConfig.newChannel();

        channel.basicQos(3);
        channel.basicConsume("normalQueue", new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(  "normalQueue "+ consumerTag +" : " + new String(body));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });


        Channel channel1 = RabbitConfig.newChannel();

        channel1.basicConsume("unRoutedQueue", new DefaultConsumer(channel1) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(  "unRoutedQueue "+ consumerTag +" : " + new String(body));
                channel1.basicAck(envelope.getDeliveryTag(), false);
            }
        });


        TimeUnit.MINUTES.sleep(10);



        channel.getConnection().close();
        channel1.getConnection().close();


    }
}
