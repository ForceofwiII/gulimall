package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    String secKill(String killId, String key, Integer num);
}
