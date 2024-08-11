package com.atguigu.gulimall.order.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-ware")
public interface WareFeign {

    @PostMapping("/ware/waresku/list/hasStock")
    public Map<Long,Boolean> listHasStock(@RequestBody List<Long> skuIds);


    @GetMapping("/ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);
}
