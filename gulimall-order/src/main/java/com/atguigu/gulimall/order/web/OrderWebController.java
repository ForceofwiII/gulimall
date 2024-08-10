package com.atguigu.gulimall.order.web;


import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;

import java.util.concurrent.ExecutionException;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;



    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }


    @GetMapping("toTrade")
    public String toTrade(HttpSession session , Model model) throws ExecutionException, InterruptedException {


        MemberEntityVo memberEntityVo = (MemberEntityVo) session.getAttribute(LOGIN_USER);

        OrderConfirmVo orderConfirmVo= orderService.confirmOrder(memberEntityVo);

       model.addAttribute("confirmOrderData",orderConfirmVo);
        return "confirm";
    }


}
