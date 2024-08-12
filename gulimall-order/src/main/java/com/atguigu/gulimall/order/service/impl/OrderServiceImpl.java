package com.atguigu.gulimall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.order.Constant;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeign;
import com.atguigu.gulimall.order.feign.MemberFeign;
import com.atguigu.gulimall.order.feign.ProductFeign;
import com.atguigu.gulimall.order.feign.WareFeign;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
import org.springframework.transaction.annotation.Transactional;


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

    @Autowired
    ProductFeign productFeign;

      @Autowired
      OrderDao orderDao;


      @Autowired
    OrderItemService orderItemService;

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
        redisTemplate.opsForValue().set(Constant.USER_ORDER_TOKEN+userid, s,30, TimeUnit.SECONDS);





        return orderConfirmVo;
    }

    @Override //提交订单
    @Transactional(rollbackFor = Exception.class)
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo, Long userId) {

        //1.保证订单只能提交一次
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


        //2.创建订单
        OrderCreateTo order = createOrder(orderSubmitVo, userId);
        //2、验证价格
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = orderSubmitVo.getPayPrice();

        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) > 0.01){
            responseVo.setCode(2);
            return responseVo;
        }

        //保存订单
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItems();

        orderEntity.setModifyTime(new Date());
        orderDao.insert(orderEntity);
        orderItemService.saveBatch(orderItems);
        //3.锁定库存

        WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
        wareSkuLockVo.setOrderSn(orderEntity.getOrderSn());
        List<OrderItemVo> collect = orderItems.stream().map((o) -> {

            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(o.getSkuId());
            orderItemVo.setCount(o.getSkuQuantity());
            orderItemVo.setTitle(o.getSpuName());


            return orderItemVo;
        }).collect(Collectors.toList());

          wareSkuLockVo.setLocks(collect);

        R r = wareFeign.orderLockStock(wareSkuLockVo);
        if(r.getCode()!=0){
            //锁定失败
            responseVo.setCode(3);
            return responseVo;
        }
        responseVo.setCode(0);
        responseVo.setOrder(orderEntity);


        return responseVo;

    }

    //创建订单
    public OrderCreateTo createOrder(OrderSubmitVo orderSubmitVo, Long userId){

        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //雪花算法生成订单号
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        long l = snowflake.nextId();
        OrderEntity order = new OrderEntity();
        order.setOrderSn(String.valueOf(l));
        //获取用户的收货地址
        R r = wareFeign.getFare(orderSubmitVo.getAddrId());
        FareVo fare = r.getData(new TypeReference<FareVo>() {
        });

        orderCreateTo.setFare(fare.getFare());
        order.setMemberId(userId);
        order.setFreightAmount(fare.getFare());
        order.setReceiverDetailAddress(fare.getAddress().getDetailAddress());
        order.setReceiverCity(fare.getAddress().getCity());
        order.setReceiverName(fare.getAddress().getName());
        order.setReceiverPhone(fare.getAddress().getPhone());
        order.setReceiverPostCode(fare.getAddress().getPostCode());
        order.setReceiverProvince(fare.getAddress().getProvince());
        order.setReceiverRegion(fare.getAddress().getRegion());
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());



        //获取用户的购物车信息
        List<OrderItemVo> cartItems = cartFeign.getCartItems(userId);
        List<OrderItemEntity> collect = new ArrayList<>();

        if(cartItems!=null && cartItems.size()>0){

            collect = cartItems.stream().map((o) -> {
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setSkuId(o.getSkuId());
                orderItemEntity.setSkuName(o.getTitle());
                orderItemEntity.setSkuAttrsVals(String.join(",",o.getSkuAttrValues()));
                orderItemEntity.setSkuQuantity(o.getCount());
                orderItemEntity.setSkuPrice(o.getPrice());
                orderItemEntity.setSkuPic(o.getImage());
                orderItemEntity.setGiftIntegration(o.getPrice().intValue());
                orderItemEntity.setGiftGrowth(o.getPrice().intValue());
                SpuInfoEntity spuInfoEntity = productFeign.infoBySkuId(o.getSkuId());
                orderItemEntity.setSpuId(spuInfoEntity.getId());
                orderItemEntity.setSpuName(spuInfoEntity.getSpuName());
                orderItemEntity.setCategoryId(spuInfoEntity.getCatalogId());
                orderItemEntity.setSpuBrand(spuInfoEntity.getBrandId().toString());

                orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
                orderItemEntity.setCouponAmount(new BigDecimal("0"));

                orderItemEntity.setPromotionAmount(new BigDecimal("0"));
                orderItemEntity.setRealAmount(orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())));




                orderItemEntity.setOrderSn(order.getOrderSn());
                return orderItemEntity;
            }).collect(Collectors.toList());

            orderCreateTo.setOrderItems(collect);
        }



        //计算价格

        computePrice(order,collect);


        orderCreateTo.setOrder(order);
        orderCreateTo.setOrderItems(collect);
        orderCreateTo.setPayPrice(order.getPayAmount());













        return  orderCreateTo;



    }

    /**
     * 计算价格的方法
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

}