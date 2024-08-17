package com.atguigu.gulimall.member.feign;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.vo.OrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-order")
public interface OrderFeign {


    @PostMapping("/select/{memberId}")
    public R select(@PathVariable("memberId") Long memberId , @RequestBody Map<String,Object> params);


}
