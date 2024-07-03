package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = GulimallProductApplication.class)
public class Test {




    @Autowired
    private BrandService brandService;

    @org.junit.Test
    public void test(){
        System.out.println("1");
    }
}
