package com.atguigu.gulimall.product.feign;


import com.atguigu.common.to.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {


    @PostMapping("/ware/waresku/hasstock")
    public List<SkuHasStockVo> hasstock(@RequestBody List<Long> ids);
}
