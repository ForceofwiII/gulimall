package com.atguigu.gulimall.product.feign;


import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

@FeignClient("gulimall-search")
public interface EsFeignService {

    @PostMapping("/search/product")
    public R up(@RequestBody List<SkuEsModel> models) ;
}
