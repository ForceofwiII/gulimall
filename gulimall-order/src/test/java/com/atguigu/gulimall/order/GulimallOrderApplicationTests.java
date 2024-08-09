package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    OrderService orderService;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    public void contextLoads() {


        DirectExchange directExchange = new DirectExchange("hello.direct", true, false);



        amqpAdmin.declareExchange(directExchange);


        Queue queue = new Queue("hello.queue", true, false, false);

        amqpAdmin.declareQueue(queue);


        amqpAdmin.declareBinding(new Binding("hello.queue", Binding.DestinationType.QUEUE, "hello.direct","hello" , null));



    }


    @Test
    public  void testSend(){

        OrderEntity order = new OrderEntity();
        order.setOrderSn("123456");
        order.setId(1L);
        order.setBillContent("测试订单");
        rabbitTemplate.convertAndSend("hello.direct","hello",order);




    }




}
