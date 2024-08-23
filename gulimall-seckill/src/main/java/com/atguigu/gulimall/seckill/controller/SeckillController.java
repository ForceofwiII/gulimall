package com.atguigu.gulimall.seckill.controller;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {


    @Autowired
    SeckillService seckillService;



    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){


     List<SeckillSkuRedisTo> tos=   seckillService.getCurrentSeckillSkus();

        return R.ok().setData(tos);
    }




    @GetMapping("/kill")
    public R secKill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num) throws InterruptedException {


     String orderSn=   seckillService.secKill(killId,key,num);


        return R.ok().setData(orderSn);
    }


}
