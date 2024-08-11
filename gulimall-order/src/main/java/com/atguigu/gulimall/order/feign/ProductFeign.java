package com.atguigu.gulimall.order.feign;


import com.atguigu.gulimall.order.vo.SpuInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeign {
    @GetMapping("/product/spuinfo/{skuId}/skuinfo")
    public SpuInfoEntity infoBySkuId(@PathVariable("skuId") Long skuId);
}
