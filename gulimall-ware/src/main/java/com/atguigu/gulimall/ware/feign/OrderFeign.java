package com.atguigu.gulimall.ware.feign;


import com.atguigu.gulimall.ware.vo.OrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeign {


    @GetMapping("/order/order/infos/{orderSn}")
    public OrderEntity getOrder(@PathVariable("orderSn")  String orderSn);
}
