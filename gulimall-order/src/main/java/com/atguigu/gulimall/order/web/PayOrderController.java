package com.atguigu.gulimall.order.web;


import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class PayOrderController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderItemService orderItemService;



    @ResponseBody
    @GetMapping( value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {


        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);
        OrderEntity orderEntity = orderService.getOrderByOrderSn(orderSn);
        payVo.setTotal_amount(orderEntity.getPayAmount().toString());
        payVo.setSubject("谷粒商城订单支付");
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        String body="";
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            body+=orderItemEntity.getSkuName()+",";
        }

        payVo.setBody(body);
        String pay = alipayTemplate.pay(payVo);


        // 设置返回的内容为HTML
        return   pay ;

    }

}
