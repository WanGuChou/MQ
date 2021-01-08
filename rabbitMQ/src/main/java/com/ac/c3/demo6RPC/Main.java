package com.ac.c3.demo6RPC;

import com.ac.conf.RabbitConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * class description<br/>
 * https://www.rabbitmq.com/priority.html
 *
 * @author wangguocheng
 * @version 1.0
 * @date 2020/12/30 20:54
 * @since JDK 1.8+
 */

public class Main {

    public static final String RPC_QUEUE_NAME = "queue.rpc";


    static {
        try {
            System.out.println("init ...");
            Channel channel = RabbitConfig.newChannel();
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);

            channel.getConnection().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            new Thread(new RPCServer()).start();
            RPCClient client = new RPCClient();


            for (int i = 0; i < 10; i++) {
                String response = client.call(i + "");
                System.out.println(i + "  ->  " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ;
    }


}


class RPCServer implements Runnable {


    static int fib(int n) {
        return n * n;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {


            Channel channel = RabbitConfig.newChannel();
            channel.basicQos(1);
            System.out.println(" Awaiting RPC requests ");

            Object monitor = new Object();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();
                String response = "";
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    int n = Integer.parseInt(message);
                    response += fib(n);
                } catch (RuntimeException e) {
                 e.printStackTrace();
                } finally {
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(Main.RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
            }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        System.out.println("等待。。。");
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            System.out.println("server 启动失败");
        }
    }

}

class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;

    public RPCClient() throws IOException {
        connection = RabbitConfig.newConnection();
        channel = connection.createChannel();
        channel.basicQos(1);
    }

    String call(String number) throws Exception {

        String replyQueueName=channel.queueDeclare().getQueue();

        String correlation = UUID.randomUUID().toString();
        AMQP.BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .correlationId(correlation)
                .replyTo(replyQueueName)
                .build();

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        channel.basicPublish("", Main.RPC_QUEUE_NAME, properties, number.getBytes(Charset.defaultCharset()));

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlation)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {

        });
        String result = response.take();
        channel.basicCancel(ctag);
        return result;
    }


    @Override
    public void close() throws Exception {
        connection.close();
    }
}
