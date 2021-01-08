package com.ac.c3.demo5priority;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * class description<br/>
 * https://www.rabbitmq.com/priority.html
 *
 * @author wangguocheng
 * @version 1.0
 * @date 2020/12/30 20:54
 * @since JDK 1.8+
 */

public class RabbitProducer {


    public static void main(String[] args) {
        try {
            priority();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The message priority field is defined as an unsigned byte, so in practice priorities should be between 0 and 255.
     * 优先级
     * 如果需要优先级队列，建议使用1到10之间的队列。当前，使用更多优先级将通过使用更多的Erlang进程消耗更多的CPU资源。 运行时调度也将受到影响
     *
     * @throws Exception
     */
    public static void priority() throws Exception {
        Connection connection = RabbitConfig.newConnection();

        Channel channel = connection.createChannel();

        Map<String, Object> args = new HashMap(1);

        args.put("x-max-priority", 20);

        //交换器声明
        channel.exchangeDeclare("ex.priority", BuiltinExchangeType.FANOUT);
        //队列声明
        channel.queueDeclare("queue.priority", true, false, false, args);
        //绑定
        channel.queueBind("queue.priority", "ex.priority", "rk.priority");


        String message = "优先级 ." + LocalDateTime.now();
        for (int i = 0; i < 20; i++) {
            channel.basicPublish("ex.priority", "", new AMQP.BasicProperties().builder().priority(i).build(),
            (i + message).getBytes());
        }
        System.out.println(LocalDateTime.now() + "发送结束");

        consumer();

        TimeUnit.SECONDS.sleep(60);
        System.out.println(LocalDateTime.now() + "--end");
        channel.close();
        connection.close();
    }


    public static void consumer() throws IOException {
        Channel consumer = RabbitConfig.newChannel();
        consumer.basicQos(1);
        consumer.basicConsume("queue.priority", new DefaultConsumer(consumer) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(LocalDateTime.now() + " 收到: " + new String(body));
                consumer.basicAck(envelope.getDeliveryTag(), true);
            }
        });
    }
}
