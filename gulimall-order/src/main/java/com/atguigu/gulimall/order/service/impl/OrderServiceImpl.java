package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.order.feign.CartFeign;
import com.atguigu.gulimall.order.feign.MemberFeign;
import com.atguigu.gulimall.order.feign.WareFeign;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
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


        //todo 防重令牌

        return orderConfirmVo;
    }

}