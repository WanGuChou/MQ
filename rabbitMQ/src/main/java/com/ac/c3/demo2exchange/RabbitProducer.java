package com.ac.c3.demo2exchange;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

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
            pusblish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 备份交换器，英文名称为 Alternate Exchange，简称庙，或者更直白地称之为“备胎交换器”。
     * 生产者在发送消息的时候如果不设置 mandatory 参数，那么消息在未被路由的情况下将会丢失：
     * 如果设置了 mandatory 参数，那么需要添加 ReturnListener 的编程逻辑，生产者的代码将
     * 变得复杂。如果既不想复杂化生产者的编程逻辑，又不想消息丢失，那么可以使用备份交换器，
     * 这样可以将未被路由的消息存储在 RabbitMQ 中，再在需要的时候去处理这些消息。
     * 可以通过在声明交换器（调用 channel . exchangeDeclare 方法）的时候添加
     * alternate-exchange 参数来实现，也可以通过策略（ Policy，详细参考 6.3 节〉的方式实现。
     * 如果两者同时使用，则前者的优先级更高，会覆盖掉 Policy 的设置。
     *
     * Fanout Exchange – 不处理路由键。你只需要简单的将队列绑定到交换机上。一个发送到交换机的消息都会被转发到与该交换机绑定的所有队列上。很像子网广播，每台子网内的主机都获得了一份复制的消息。Fanout交换机转发消息是最快的。
     *
     *
     * 1 如果设置的备份交换器不存在，客户端和 RabbitMQ 服务端都不会有异常出现，此时消
     *   息会丢失。
     * 2 如果备份交换器没有绑定任何队列，客户端和 RabbitMQ 服务端都不会有异常出现，此
     *   时消息会丢失。
     * 3 如果备份交换器没有任何匹配的队列，客户端和 RabbitMQ 服务端都不会有异常出现，
     *   此时消息会丢失。
     * 4 如果备份交换器和 mandatory 参数一起使用，那么 mandatory 参数无效。
     *
     * @throws Exception
     */
    public static void pusblish() throws Exception {
        Connection connection = RabbitConfig.newConnection();
        Channel channel = connection.createChannel();
        Map<String,Object> args=new HashMap();

        // 设置备份交换器 为 myAe
        args.put("alternate-exchange","myAe");

        channel.exchangeDeclare("normalExchange", BuiltinExchangeType.DIRECT, true, false, args);
        channel.exchangeDeclare("myAe", BuiltinExchangeType.FANOUT, true, false, null);


        channel.queueDeclare("normalQueue", true, false, false, null);
        channel.queueBind("normalQueue","normalExchange","normalKey");


        channel .queueDeclare ("unRoutedQueue", true, false , false, null);
        channel .queueBind ("unRoutedQueue","myAe","");



        channel.basicPublish("normalExchange", "normalKey", MessageProperties.PERSISTENT_TEXT_PLAIN, "正确的key".getBytes());
        channel.basicPublish("normalExchange", "normalKeyNo", MessageProperties.PERSISTENT_TEXT_PLAIN, "错误的key".getBytes());




        channel.close();
        connection.close();
    }






}
