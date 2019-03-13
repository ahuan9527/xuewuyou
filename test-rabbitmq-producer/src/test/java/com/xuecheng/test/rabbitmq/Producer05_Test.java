package com.xuecheng.test.rabbitmq;

import com.xuecheng.test.rabbitmq.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_Test {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void test01(){
        String msg = "send email";
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TOPICS_INFORM,"inform.email",msg);
    }
}
