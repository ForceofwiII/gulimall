package com.atguigu.gulimall.order.listener;


import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class OrderCancel {

    @Autowired
    private OrderService orderService;

    @Autowired
    OrderDao orderDao;


    @RabbitListener(queues = "order.release.order.queue")
    public void cancelOrder(Message message){




        OrderEntity order = JSON.parseObject(message.getBody(), OrderEntity.class);
        System.out.println("收到消息" + order);
        String orderSn = order.getOrderSn();
        OrderEntity order1 = orderService.getOrderByOrderSn(orderSn);
        if(order1==null){
            return;
        }
        if(order1.getStatus()==0){
          orderDao.updateBySn(order1.getOrderSn(),4);
        }


    }
}
