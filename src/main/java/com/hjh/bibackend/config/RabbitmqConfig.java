package com.hjh.bibackend.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
@Slf4j
public class RabbitmqConfig {
    //定义BI队列
    public static final String QUEUE_BI = "queue_bi";
    //定义BI交换机
    public static final String EXCHANGE_BI ="exchange_bi";
    //定义路由键
    public static final String ROUTINGKEY_BI ="routing.bi";

    //定义死信队列
    public static final String QUEUE_BI_DLX_QUEUE = "queue_bi_dlx_queue";
    //定义死信交换机
    public static final String EXCHANGE_BI_DLX_EXCHANGE = "exchange_bi_dlx_exchange";
    //绑定
    public static final String ROUTINGKEY_DLX_BI ="routing.dlx.bi";


    //声明交换机
    @Bean(EXCHANGE_BI)
    public Exchange EXCHANGE_BI(){
        return new DirectExchange(EXCHANGE_BI,true,false);
    }

    //声明死信交换机
    @Bean(EXCHANGE_BI_DLX_EXCHANGE)
    public Exchange EXCHANGE_BI_DLX_EXCHANGE(){
        return new DirectExchange(EXCHANGE_BI_DLX_EXCHANGE,true,false);
    }

    //声明队列
    @Bean(QUEUE_BI)
    public Queue QUEUE_BI(){
        Map<String, Object> args = new HashMap<>(3);
        //正常队列中的消息被废弃后会被路由到死信队列(前提是有绑定死信队列)
        // 绑定我们的死信交换机
        args.put("x-dead-letter-exchange", EXCHANGE_BI_DLX_EXCHANGE);
        // 绑定我们的路由key
        args.put("x-dead-letter-routing-key", ROUTINGKEY_DLX_BI);
        args.put("x-message-ttl",1800000);

        return new Queue(QUEUE_BI,true,false,false,args);
    }

    //声明死信队列
    @Bean(QUEUE_BI_DLX_QUEUE)
    public Queue QUEUE_BI_DLX_QUEUE(){
        return new Queue(QUEUE_BI_DLX_QUEUE,true,false,false);
    }

    //交换机绑定队列
    @Bean
    public Binding BINDING_QUEUE_BI(){
        return new Binding(QUEUE_BI, Binding.DestinationType.QUEUE, EXCHANGE_BI, ROUTINGKEY_BI,null);
    }

    //死信交换机绑定死信队列
    @Bean
    public Binding BINDING_DLX_QUEUE_BI(){
        return new Binding(QUEUE_BI_DLX_QUEUE, Binding.DestinationType.QUEUE, EXCHANGE_BI_DLX_EXCHANGE, ROUTINGKEY_DLX_BI,null);
    }
}