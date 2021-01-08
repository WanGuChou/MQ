package com.ac.c3.demo1base;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

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


    public static void main(String[] args)  {


        try {

            Channel  channel = RabbitConfig.newChannel();

            channel.exchangeDeclare("exchange_auto_delete",BuiltinExchangeType.DIRECT,true,true,null);

            TimeUnit.SECONDS.sleep(60);
            channel.exchangeDelete("exchange_auto_delete");

            channel.close();
            channel.getConnection().close();
            // pusblish();
            // mandatory();
           // immediate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送
     *
     * @throws Exception
     */
    public static void pusblish() throws Exception {
        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(RabbitConfig.exchange, BuiltinExchangeType.DIRECT, true, false, null);
        channel.queueDeclare(RabbitConfig.queue, true, false, false, null);
        channel.queueBind(RabbitConfig.queue, RabbitConfig.exchange, RabbitConfig.routing_key);
        channel.basicPublish(RabbitConfig.exchange, RabbitConfig.routing_key, MessageProperties.PERSISTENT_TEXT_PLAIN, "12345678".getBytes());
        channel.close();
        connection.close();
    }

    /**
     * mandatory
     * <p>
     * 当 mandatory 参数设为 true 时，交换器无法根据自身的类型和路由键找到一个符合条件
     * 的队列，那么 RabbitMQ 会调用 Basic.Return 命令将消息返回给生产者。当 mandatory 参
     * 数设置为 false 时，出现上述情形，则消息直接被丢弃 。
     * </p>
     *
     * @throws Exception
     */
    public static void mandatory() throws Exception {

        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
            String message = new String(body);
            System.out.println("返回的结果：" + message + " 原因：" + replyText);

        });
        channel.basicPublish(RabbitConfig.exchange, "not-exist-queue", true, MessageProperties.PERSISTENT_TEXT_PLAIN, "mandatory".getBytes());
        TimeUnit.SECONDS.sleep(10);
        channel.close();
        connection.close();
    }

    /**
     * 不建议使用这个
     * 当 immediate 参数设为 true 时，如果交换器在将消息路由到队列时发现队列上并不存在
     * 任何消费者，那么这条消息将不会存入队列中。当与路由键匹配的所有队列都没有消费者时，
     * 该消息会通过 Basic .Return 返回至生产者。
     * 概括来说， ma口datory 参数告诉服务器至少将该消息路由到一个队列中，否则将消息返
     * 回给生产者 。 immediate 参数告诉服务器，如果该消息关联的队列上有消费者，则立刻投递：
     * 如果所有匹配的队列上都没有消费者，则直接将消息返还给生产者，不用将消息存入队列而等
     * 待消费者了。
     *
     * @throws Exception
     */
    public static void immediate() throws Exception {

        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(RabbitConfig.exchange, BuiltinExchangeType.DIRECT, true, false, null);
        channel.queueDeclare(RabbitConfig.queue, true, false, false, null);
        channel.queueBind(RabbitConfig.queue, RabbitConfig.exchange, RabbitConfig.routing_key);
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
            String message = new String(body);
            System.out.println("返回的结果：" + message + " 原因：" + replyText);

        });
        channel.basicPublish(RabbitConfig.exchange, RabbitConfig.routing_key, true, true, MessageProperties.PERSISTENT_TEXT_PLAIN, "immediate".getBytes());
        TimeUnit.SECONDS.sleep(10);
        channel.close();
        connection.close();
    }
}
