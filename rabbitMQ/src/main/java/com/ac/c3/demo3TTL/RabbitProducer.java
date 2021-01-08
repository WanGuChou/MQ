package com.ac.c3.demo3TTL;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.HashMap;
import java.util.Map;

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
          //  ttlQueue();
            queueTtl();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息的 TTL
     *
     * 目前有两种方法可以设置。
     * 第一种
     * 通过队列属性设置，队列中所有消息都有相同的过期时间。
     * 消息过期，直接从队列中删除
     * <p>
     * 第二种
     * 对消息本身进行单独设置，每条消息的 TTL 可以不同。
     * 消息过期，不会被马上删除，而知在投递到消费者前判定
     *
     *
     * <p>
     * 如果两种方法一起使用，则消息的 TTL 以两者之间较小的那个数值为准。消息在队列中的生存时
     * I司 一旦超过设置 的 TTL 值时，就会变成“死信”（ Dead Message ），消费者将无法再收到该消息
     *
     * @throws Exception
     */
    public static void ttlQueue() throws Exception {
        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> args = new HashMap();

        // 设置备份交换器 为 myAe
        args.put("x-message-ttl", 6000);

        channel.queueDeclare("ttlQueue", true, false, false, args);

        channel.close();
        connection.close();
    }


    public static void ttlMessage() throws Exception {
        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().deliveryMode(2).expiration("60000").build();
        channel.basicPublish("", "", properties, "消息失效".getBytes());
        channel.close();
        connection.close();
    }


    /**
     * 队列的TTL
     * @throws Exception
     */
    public static void queueTtl() throws Exception {
        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> args = new HashMap();
        args.put("x-expires", 60000);
        channel.queueDeclare("x-expires-queue", true, false, false, args);

        channel.close();
        connection.close();
    }




}
