package com.atguigu.gulimall.order.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-ware")
public interface WareFeign {

    @PostMapping("/ware/waresku/list/hasStock")
    public Map<Long,Boolean> listHasStock(@RequestBody List<Long> skuIds);
}
