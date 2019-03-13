package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer03_topics_sms {

    //队列名称
    private static final String QUEUE_INFORM_SMS="queue_inform_sms";
    private static final String EXCHANGE_TOPICS_INFORM="exchange_topics_inform";
    private static final String INFORM_SMS="inform.#.sms.#";
    public static void main(String[] args) {
        Connection connection = null;
        Channel channel = null;

        try {  //创建连接
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.43.9");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setVirtualHost("/");//rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务
            //创建与RabbitMQ服务的TCP连接
            connection = factory.newConnection(); //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
            channel = connection.createChannel();
            //声明交换机
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //声明队列
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            //绑定队列
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_TOPICS_INFORM,INFORM_SMS);

            //消费方法
            DefaultConsumer consumer = new DefaultConsumer(channel) {

                /**
                 * 消费者接收消息调用此方法
                 * @param consumerTag 消费者的标签，在channel.basicConsume()去指定
                 * @param envelope 消息包的内容，可从中获取消息id，消息routingkey，交换机，消息和重传标志
                 * (收到消息失败后是否需要重新发送)
                 * @param properties 消息属性
                 * @param body 消息内容
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //路由key
                    String routingKey = envelope.getRoutingKey();
                    //消息id
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String msg = new String(body, "utf-8");
                    System.out.println("receive  message.." + msg);
                }
            };
            //监听队列
            channel.basicConsume(QUEUE_INFORM_SMS,true,consumer);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
