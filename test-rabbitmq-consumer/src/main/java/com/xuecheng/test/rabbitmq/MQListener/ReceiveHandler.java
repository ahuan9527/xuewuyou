package com.xuecheng.test.rabbitmq.MQListener;

import com.rabbitmq.client.Channel;
import com.xuecheng.test.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveHandler {

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_INFORM_EMAIL})
    public void receiveHandler(String msg, Message message, Channel channel){
        System.out.println(msg);
    }
}
