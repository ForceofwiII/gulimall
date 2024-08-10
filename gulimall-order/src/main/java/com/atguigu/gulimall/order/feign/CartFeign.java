package com.atguigu.gulimall.order.feign;


import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeign {


    @GetMapping("/{userId}/getCartItems")
    public List<OrderItemVo> getCartItems(@PathVariable("userId") Long userId);
}
