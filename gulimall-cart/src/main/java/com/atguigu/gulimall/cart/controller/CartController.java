package com.atguigu.gulimall.cart.controller;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {


    @GetMapping("/cart.html")
    public String cartListPage(){

        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();


        return "cartList";
    }


}
