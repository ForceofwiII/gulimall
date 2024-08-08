package com.atguigu.gulimall.cart.controller;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {



    @Autowired
    CartService cartService;


    @GetMapping("/cart.html") //查询购物车
    public String cartListPage(){

        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        if(userInfoTo.getUserId()==null){
            return "redirect:http://auth.gulimall.com/login.html";
        }

        return "cartList";
    }


    @GetMapping("/addCartItem") //添加商品到购物车
    public  String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, Model model) throws ExecutionException, InterruptedException {
        if(CartInterceptor.toThreadLocal.get().getUserId()==null){
            return "redirect:http://auth.gulimall.com/login.html";
        }

        CartItemVo cartItemVo = cartService.addToCart(skuId,num);
        model.addAttribute("cartItem",cartItemVo);



        return "success";
    }


}
