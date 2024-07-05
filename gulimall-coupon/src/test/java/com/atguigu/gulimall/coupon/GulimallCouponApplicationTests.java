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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@RefreshScope
public class GulimallCouponApplicationTests {


    @Autowired
    CouponService couponService;


    @Test
    public void contextLoads() {



       List<Integer> a  = new ArrayList<>();
         a.add(1);
            a.add(2);
            a.add(3);
        List<Integer> collect = a.stream().map((o) -> {
            return o + 1;
        }).collect(Collectors.toList());
        System.out.println(collect);

    }

}
