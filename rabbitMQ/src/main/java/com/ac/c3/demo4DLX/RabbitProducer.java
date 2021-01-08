package com.ac.c3.demo4DLX;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * class description<br/>
 *
 * @author wangguocheng
 * @version 1.0
 * @date 2020/12/30 20:54
 * @since JDK 1.8+
 */

public class RabbitProducer {


    public static void main(String[] args) {
        try {
            dlx();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息的 TTL
     * <p>
     * DLX，全称为 Dead-Letter-Exchange，可以称之为死信交换器，也有人称之为死信邮箱。当
     * 消息在一个队列中变成死信（ dead message ）之后，它能被重新被发送到另一个交换器中，这个
     * 交换器就是 DLX，绑定 DLX 的队列就称之为死信队列。
     * 消息变成死信一般是由于以下几种情况：
     * 1.消息被拒绝（ Basic.Reject/Basic.Nack ），井且设置 requeue 参数为 false;
     * 2.消息过期；
     * 3.队列达到最大长度。
     * DLX 也是一个正常的交换器，和一般的交换器没有区别，它能在任何的队列上被指定，实
     * 际上就是设置某个队列的属性。当这个队列中存在死信时 ， RabbitMQ 就会自动地将这个消息重
     * 新发布到设置的 DLX 上去，进而被路由到另一个队列，即死信队列。可以监听这个队列中的消
     * 息、以进行相应的处理，这个特性与将消息的 TTL 设置为 0 配合使用可以弥补 immediate 参数
     * 的功能。
     * <p>
     * 关键字
     * x-dead-letter-exchange
     * x-dead-letter-routing-key
     *
     * @throws Exception
     */
    public static void dlx() throws Exception {
        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> args = new HashMap(1);
        args.put("x-message-ttl", 10000);
        args.put("x-dead-letter-exchange", "ex.dlx");
        args.put("x-dead-letter-routing-key", "rk.dlx");



        //交换器声明
        channel.exchangeDeclare("ex.normal", BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare("ex.dlx", BuiltinExchangeType.DIRECT);

        //队列声明
        channel.queueDeclare("queue.normal", true, false, false, args);
        channel.queueDeclare("queue.dlx", true, false, false, null);

        //绑定

        channel.queueBind("queue.normal", "ex.normal", "rk.normal");
        channel.queueBind("queue.dlx", "ex.dlx", "rk.dlx");




        String message="死信消息 ."+LocalDateTime.now();
        for (int i = 0; i < 20; i++) {
            channel.basicPublish("ex.normal", "rk.normal", MessageProperties.PERSISTENT_TEXT_PLAIN, (i+message).getBytes());
        }
        System.out.println(LocalDateTime.now()+"发送");


        consumer();

        TimeUnit.SECONDS.sleep(60);
        System.out.println(LocalDateTime.now()+"--end");
        channel.close();
        connection.close();
    }





    public static void  consumer() throws IOException {
        Channel consumer = RabbitConfig.newChannel();
        consumer.basicQos(10);
        consumer.basicConsume("queue.dlx", new DefaultConsumer(consumer) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(LocalDateTime.now() + " 收到: " + new String(body));
                consumer.basicAck(envelope.getDeliveryTag(), true);
            }
        });
    }
}
