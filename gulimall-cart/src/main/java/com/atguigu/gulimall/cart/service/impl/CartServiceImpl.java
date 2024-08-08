package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeign;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CartServiceImpl implements CartService {

  @Autowired
  StringRedisTemplate redisTemplate;

  @Autowired
  ProductFeign productFeign;

  public static final String CART_PREFIX = "gulimall:cart:";

  @Autowired
  ThreadPoolExecutor executor;




  @Override
  public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

    String cartKey = CART_PREFIX + CartInterceptor.toThreadLocal.get().getUserId();
    //如果购物车有 只需要修改数量
    BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

    String o = (String) operations.get(skuId.toString());
    if(!StringUtils.isEmpty(o)){
      CartItemVo cartItemVo = JSON.parseObject(o, CartItemVo.class);
        cartItemVo.setCount(cartItemVo.getCount()+num);
        cartItemVo.setTotalPrice(cartItemVo.getPrice().multiply(new BigDecimal(cartItemVo.getCount())));

        operations.put(skuId.toString(),JSON.toJSONString(cartItemVo));



        return  cartItemVo;

    }
    //购物车没有 新加入
    CartItemVo cartItemVo = new CartItemVo();

    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
      R skuInfo = productFeign.info(skuId);
      SkuInfoVo skuInfo1 = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
      });


      cartItemVo.setSkuId(skuId);
      cartItemVo.setTitle(skuInfo1.getSkuTitle());
      cartItemVo.setImage(skuInfo1.getSkuDefaultImg());
      cartItemVo.setPrice(skuInfo1.getPrice());
      cartItemVo.setCount(num);
      cartItemVo.setTotalPrice(cartItemVo.getPrice().multiply(new BigDecimal(cartItemVo.getCount())));

    }, executor);


    CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
      List<String> strings = productFeign.stringList(skuId);
      cartItemVo.setSkuAttrValues(strings);

    }, executor);


    //等待所有任务都完成
    CompletableFuture.allOf(future,future1).get();




    operations.put(skuId.toString(), JSON.toJSONString(cartItemVo));



    return cartItemVo;
  }

  @Override
  public CartVo getCart() {

    Long userId = CartInterceptor.toThreadLocal.get().getUserId();
    String cartKey = CART_PREFIX + userId;
    CartVo cartVo = new CartVo();
    BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
    List<Object> values = operations.values();

    List<CartItemVo> collect = values.stream().map((o) -> {
      return JSON.parseObject((String) o, CartItemVo.class);

    }).collect(Collectors.toList());

    cartVo.setItems(collect);

    cartVo.setCountNum(cartVo.getCountNum());
    cartVo.setCountType(cartVo.getCountType());
    cartVo.setTotalAmount(cartVo.getTotalAmount());
    return cartVo;


  }


  @Override
  public void deleteCart(){
    Long userId = CartInterceptor.toThreadLocal.get().getUserId();
    String cartKey = CART_PREFIX + userId;
    redisTemplate.delete(cartKey);
  }

  @Override
  public void checkItem(Long skuId, Integer checked) {
    Long userId = CartInterceptor.toThreadLocal.get().getUserId();
    String cartKey = CART_PREFIX + userId;
    String o = (String) redisTemplate.opsForHash().get(cartKey, skuId.toString());

    CartItemVo cartItemVo = JSON.parseObject(o, CartItemVo.class);
    cartItemVo.setCheck(checked==1);
    redisTemplate.opsForHash().put(cartKey,skuId.toString(),JSON.toJSONString(cartItemVo));


  }

  @Override
  public void countItem(Long skuId, Integer num) {

    Long userId = CartInterceptor.toThreadLocal.get().getUserId();
    String cartKey = CART_PREFIX + userId;
    String o = (String) redisTemplate.opsForHash().get(cartKey, skuId.toString());
    CartItemVo cartItemVo = JSON.parseObject(o, CartItemVo.class);
    cartItemVo.setCount(num);

    redisTemplate.opsForHash().put(cartKey,skuId.toString(),JSON.toJSONString(cartItemVo));



  }

  @Override
  public void deleteItem(Long skuId) {
    Long userId = CartInterceptor.toThreadLocal.get().getUserId();
    String cartKey = CART_PREFIX + userId;
    redisTemplate.opsForHash().delete(cartKey,skuId.toString());

  }


}
