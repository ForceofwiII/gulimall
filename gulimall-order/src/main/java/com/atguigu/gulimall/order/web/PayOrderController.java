package com.atguigu.gulimall.order.web;


import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PayOrderController {

    @Autowired
    AlipayTemplate alipayTemplate;


    @GetMapping("/payOrder")
    public String payOrder(@RequestParam("orderSn") String orderSn){


        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);




        return "pay";
    }

}
