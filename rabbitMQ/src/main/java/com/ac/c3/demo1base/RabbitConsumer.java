package com.ac.c3.demo1base;

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
        Consumer consumer= new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(   consumerTag +" : " + new String(body));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicQos(3);
        channel.basicConsume(RabbitConfig.queue, consumer);

        TimeUnit.MINUTES.sleep(10);
        channel.close();
        channel.getConnection().close();


    }
}
