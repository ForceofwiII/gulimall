package com.atguigu.gulimall.coupon;

import com.atguigu.gulimall.coupon.dao.CouponDao;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@RefreshScope
public class GulimallCouponApplicationTests {


    @Autowired
    CouponService couponService;

    @Value("${test.t1}")
    private String t1;
    @Test
    public void contextLoads() {



        System.out.println(t1);

    }

}
