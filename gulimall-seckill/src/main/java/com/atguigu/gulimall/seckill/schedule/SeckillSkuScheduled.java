package com.atguigu.gulimall.seckill.schedule;


import com.atguigu.gulimall.seckill.service.SeckillService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SeckillSkuScheduled {


    @Autowired
    SeckillService seckillService;

  //每天晚上3点上架最近三天需要秒杀的商品

    @XxlJob("uploadSeckillSkuLatest3Days") //定时任务名 jobHandler

    public void uploadSeckillSkuLatest3Days() {
        log.info("上架秒杀商品最新3天的商品");

        seckillService.uploadSeckillSkuLatest3Days();
        System.out.println("test");
    }







}
