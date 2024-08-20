package com.atguigu.gulimall.seckill.service.impl;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionWithSkusVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class SeckillServiceImpl  implements SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String SESSION__CACHE_PREFIX = "seckill:sessions:";

    private final String SECKILL_CHARE_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码


    @Override
    public void uploadSeckillSkuLatest3Days() {


        R lates3DaySession = couponFeignService.getLates3DaySession();
        if (lates3DaySession.getCode() == 0) {
            //上架商品
            List<SeckillSessionWithSkusVo> sessionData = lates3DaySession.getData("data", new TypeReference<List<SeckillSessionWithSkusVo>>() {
            });
            //缓存到Redis
            //1、缓存活动信息
            saveSessionInfos(sessionData);

            //2、缓存活动的关联商品信息
            saveSessionSkuInfo(sessionData);
        }

    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        //1、确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSION__CACHE_PREFIX + "*");
        for (String key : keys) {
            //key的格式：seckill:sessions:startTime_endTime
            String replace = key.replace(SESSION__CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if(time>=start && time<=end){
                //获取到当前场次的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
                List<SeckillSkuRedisTo> collect = range.stream().map(k -> {
                    String json = (String) ops.get(k);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //设置当前商品的秒杀开始时间和结束时间
                    redisTo.setStartTime(start);
                    redisTo.setEndTime(end);
                    redisTo.setRandomCode(null);
                    return redisTo;
                }).collect(Collectors.toList());
                return collect;
            }

        }


        return null;
    }

    @Override
    public String secKill(String killId, String key, Integer num) {

        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        String s = operations.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
        //校验合法性
        if(seckillSkuRedisTo.getRandomCode()!=key){
            return null;
        }
        //校验时间的合法性
        long time = new Date().getTime();
        if(time>=seckillSkuRedisTo.getStartTime() && time<=seckillSkuRedisTo.getEndTime()){
            //校验购物数量是否合法
            if(num<=seckillSkuRedisTo.getSeckillLimit()){
                //校验库存是否足够
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);
                boolean b = semaphore.tryAcquire(num);
                if(b){
                    //秒杀成功
                    //快速下单，发送MQ消息
                    String orderSn = UUID.randomUUID().toString().replace("-", "");
                    rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderSn);
                    return orderSn;
                }
            }
        }

        return null;
    }

    /**
     * 缓存秒杀活动信息
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionWithSkusVo> sessions) {

        sessions.stream().forEach(session -> {

            //获取当前活动的开始和结束时间的时间戳
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();

            //存入到Redis中的key
            String key = SESSION__CACHE_PREFIX + startTime + "_" + endTime;

            //判断Redis中是否有该信息，如果没有才进行添加
            Boolean hasKey = redisTemplate.hasKey(key);
            //缓存活动信息
            if (!hasKey) {
                //获取到活动中所有商品的skuId
                List<String> skuIds = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "-" + item.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key,skuIds);
            }
        });

    }

    /**
     * 缓存秒杀活动所关联的商品信息
     * @param sessions
     */
    private void saveSessionSkuInfo(List<SeckillSessionWithSkusVo> sessions) {

        sessions.stream().forEach(session -> {
            //准备hash操作，绑定hash
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                //生成随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                String redisKey = seckillSkuVo.getPromotionSessionId().toString() + "-" + seckillSkuVo.getSkuId().toString();
                if (!operations.hasKey(redisKey)) {

                    //缓存我们商品信息
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    Long skuId = seckillSkuVo.getSkuId();
                    //1、先查询sku的基本信息，调用远程服务
                    R info = productFeignService.getSkuInfo(skuId);
                    if (info.getCode() == 0) {
                        SkuInfoVo skuInfo = info.getData("skuInfo",new TypeReference<SkuInfoVo>(){});
                        redisTo.setSkuInfo(skuInfo);
                    }

                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);

                    //3、设置当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    //4、设置商品的随机码（防止恶意攻击）
                    redisTo.setRandomCode(token);

                    //序列化json格式存入Redis中
                    String seckillValue = JSON.toJSONString(redisTo);
                    operations.put(seckillSkuVo.getPromotionSessionId().toString() + "-" + seckillSkuVo.getSkuId().toString(),seckillValue);

                    //如果当前这个场次的商品库存信息已经上架就不需要上架
                    //5、使用库存作为分布式Redisson信号量（限流）
                    // 使用库存作为分布式信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }

}
