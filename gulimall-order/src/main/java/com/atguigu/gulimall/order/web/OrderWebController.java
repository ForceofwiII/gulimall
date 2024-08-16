package com.atguigu.gulimall.order.web;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

import java.util.concurrent.ExecutionException;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;


    @Autowired
    RedissonClient redissonClient;



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


    @PostMapping("/submitOrder")
    public  String submitOrder(OrderSubmitVo orderSubmitVo,HttpSession session, Model model){






        MemberEntityVo memberEntityVo = (MemberEntityVo) session.getAttribute(LOGIN_USER);


        Long userId = memberEntityVo.getId();


        SubmitOrderResponseVo submitOrderResponseVo= orderService.submitOrder(orderSubmitVo,userId);


         if(submitOrderResponseVo.getCode()!=0){
             System.out.println(submitOrderResponseVo.getCode());
             return "redirect:http://order.gulimall.com/toTrade"; //订单提交失败,重定向到订单确认页
         }

         model.addAttribute("submitOrderResp",submitOrderResponseVo);



         return "pay";




    }


}
