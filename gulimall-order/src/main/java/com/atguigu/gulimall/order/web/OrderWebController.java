package com.atguigu.gulimall.order.web;


import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;



    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }


    @GetMapping("toTrade")
    public String toTrade(){

       OrderConfirmVo orderConfirmVo= orderService.confirmOrder();


        return "confirm";
    }


}
