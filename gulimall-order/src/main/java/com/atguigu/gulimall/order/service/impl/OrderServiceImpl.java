package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.order.Constant;
import com.atguigu.gulimall.order.feign.CartFeign;
import com.atguigu.gulimall.order.feign.MemberFeign;
import com.atguigu.gulimall.order.feign.WareFeign;
import com.atguigu.gulimall.order.vo.*;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeign memberFeign;

    @Autowired
    CartFeign cartFeign;


    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeign wareFeign;


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder(MemberEntityVo memberEntityVo) throws ExecutionException, InterruptedException {
        //查出收货信息
        Long userid = memberEntityVo.getId();
        OrderConfirmVo orderConfirmVo  = new OrderConfirmVo();
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            List<MemberAddressVo> address = memberFeign.getAddress(userid);
            orderConfirmVo.setMemberAddressVos(address);
        }, executor);

        //查出购物车中所有选中的购物项
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

            List<OrderItemVo> cartItems = cartFeign.getCartItems(userid);
            orderConfirmVo.setItems(cartItems);
            orderConfirmVo.setTotal();
            orderConfirmVo.setPayPrice();


        }).thenRunAsync(() -> {
            //远程查询是否有库存
            List<Long> skuIds = orderConfirmVo.getItems().stream().map((o) -> {
                return o.getSkuId();
            }).collect(Collectors.toList());
            Map<Long, Boolean> stock = wareFeign.listHasStock(skuIds);

            System.out.println(stock);

            orderConfirmVo.setStocks(stock);

        }, executor);



        orderConfirmVo.setIntegration(memberEntityVo.getIntegration());


        CompletableFuture.allOf(completableFuture,future).get();


        //todo 防重令牌保证幂等性

        String s = UUID.randomUUID().toString();

           orderConfirmVo.setOrderToken(s);
        redisTemplate.opsForValue().setIfAbsent(Constant.USER_ORDER_TOKEN+userid, s,30, TimeUnit.SECONDS);





        return orderConfirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo, Long userId) {

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        RLock rLock = redissonClient.getLock(userId.toString());
        rLock.lock();
        try{
            //1.验证令牌
            String s = redisTemplate.opsForValue().get(Constant.USER_ORDER_TOKEN + userId);
            String orderToken = orderSubmitVo.getOrderToken();
            if( s==null||  !StringUtils.equals(s,orderToken)){
                //令牌不对

                responseVo.setCode(1);
                return responseVo;
            }

            redisTemplate.delete(Constant.USER_ORDER_TOKEN+userId);


        }
        finally {
            rLock.unlock();
        }



        return null;

    }

}