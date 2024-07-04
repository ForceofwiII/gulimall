package com.atguigu.gulimall.member;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallMemberApplicationTests {

    @Autowired
    MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @Test
    public void contextLoads() {


        R membercoupons = couponFeignService.membercoupons();

        System.out.println(membercoupons);
    }

}
