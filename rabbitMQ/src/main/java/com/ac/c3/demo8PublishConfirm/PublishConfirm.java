package com.ac.c3.demo8PublishConfirm;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 生产者确认<br/>
 *
 * @author wangguocheng
 * @version 1.0
 * @date 2021/1/6 16:33
 * @since JDK 1.8+
 */
public class PublishConfirm implements AutoCloseable {

    private Channel channel = RabbitConfig.newChannel();


    /**
     * 正常流程
     * 1.Tx.Select
     * 2.Tx.Select.Ok
     * 3.Basic.Publish
     * 4.Tx.Commit
     * 5.Tx.Commit.Ok
     * <p>
     * 异常流程
     * 1.Tx.Select
     * 2.Tx.Select.Ok
     * 3.Basic.Publish
     * 4.Tx.Rollback
     * 5.Tx.Rollback.Ok
     * <p>
     * 效率最低的方式
     */
    public void transactional() {
        try {
            channel.txSelect();
            channel.basicPublish("", "queue.confirm", MessageProperties.TEXT_PLAIN, "transaction message".getBytes());
            channel.txCommit();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.txRollback();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


    }


    public void confirm() {
        try {
            channel.confirmSelect();
            channel.basicPublish("", "queue.confirm", MessageProperties.TEXT_PLAIN, "confirm message".getBytes());
            if (!channel.waitForConfirms()) {
                System.out.println("send  message failed");
            }
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchConfirm() {
        try {
            channel.confirmSelect();
            while (true) {
                channel.basicPublish("", "queue.confirm", MessageProperties.TEXT_PLAIN, "confirm message".getBytes());
                try {
                    if (channel.waitForConfirms()) {
                        System.out.println("send  message success");
                    }
                    ;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //TODO 从新发送消息
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void asyncConfirm() {
        try {
            channel.confirmSelect();
            channel.addConfirmListener((deliveryTag, multiple) -> {
                //ack
                System.out.println("asyncConfirm ack");
            }, (deliveryTag, multiple) -> {
                //nack
                System.out.println("asyncConfirm nack");

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void close() throws Exception {
        channel.getConnection().close();
    }

    public static void main(String[] args) {
        try {
            PublishConfirm confirm = new PublishConfirm();
            confirm.asyncConfirm();
            confirm.channel.exchangeDeclare("ex.confirm.async", BuiltinExchangeType.DIRECT,false,true,null);
            confirm.channel.queueDeclare("queue.confirm.async", false, false, true, null);

            confirm.channel.queueBind("queue.confirm.async","ex.confirm.async","queue.confirm.async");
            confirm.channel.basicPublish("ex.confirm.async", "queue.confirm.async", MessageProperties.TEXT_PLAIN, "asyncConfirm message".getBytes());
            new Thread(() -> {
                try {
                    Channel consumer = RabbitConfig.newChannel();
                    consumer.basicConsume("queue.confirm.async", false, (consumerTag, message) -> {
                        System.out.println("收到消息" + new String(message.getBody()));
                        consumer.basicNack(message.getEnvelope().getDeliveryTag(), true,false);

                    }, (consumerTag, sig) -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            TimeUnit.SECONDS.sleep(20);
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
