package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = GulimallProductApplication.class)
public class Test {




    @Autowired
    private BrandService brandService;

    @org.junit.Test
    public void test(){

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功....");
    }
}
