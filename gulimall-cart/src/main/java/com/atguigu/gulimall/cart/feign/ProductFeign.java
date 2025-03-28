package com.atguigu.gulimall.cart.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeign {


    @GetMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringList/{skuId}")
    public List<String> stringList(@PathVariable("skuId") Long skuId);


    @GetMapping("/product/skuinfo/{skuId}/getPrice")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId);

}
