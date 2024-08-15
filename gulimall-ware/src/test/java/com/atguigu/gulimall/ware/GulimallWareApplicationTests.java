package com.atguigu.gulimall.ware;

import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallWareApplicationTests {

       @Autowired
    PurchaseService purchaseService;

       @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void contextLoads() {





        try{
            rabbitTemplate.convertAndSend("hello.direct","hello","hello");
        }
        catch (Exception e){
            System.out.println("error");
        }


    }

}
