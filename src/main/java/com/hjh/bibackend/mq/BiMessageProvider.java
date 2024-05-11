package com.hjh.bibackend.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hjh.bibackend.config.RabbitmqConfig.EXCHANGE_BI;
import static com.hjh.bibackend.config.RabbitmqConfig.ROUTINGKEY_BI;

@Slf4j
@Component
public class BiMessageProvider {

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 发送消息
     * @param chartId
     */
    public void sendMessage(String chartId){
        rabbitTemplate.convertAndSend(EXCHANGE_BI,ROUTINGKEY_BI,chartId, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //设置消息的有效时间
            message.getMessageProperties().setExpiration("6000");
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }
}
