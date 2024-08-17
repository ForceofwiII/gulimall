package com.atguigu.gulimall.member.feign;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gulimall-order")
public interface OrderFeign {
}
