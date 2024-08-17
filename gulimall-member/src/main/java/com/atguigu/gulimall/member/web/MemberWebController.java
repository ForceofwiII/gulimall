package com.atguigu.gulimall.member.web;


import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.member.feign.OrderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;

@Controller
public class MemberWebController {

    @Autowired
    OrderFeign orderFeign;



    @GetMapping("/memberOrder.html")  //处理支付完成后的请求
    public String memberOrder(HttpSession session , Model model  , @RequestParam( value="pageNum" , defaultValue = "1") Integer pageNum){

        //分页查出当前用户的所有订单信息
        MemberEntityVo attribute =  (MemberEntityVo)   session.getAttribute(LOGIN_USER);
        Map<String,Object> page =  new HashMap<>();
        page.put("page",pageNum.toString());
        R r = orderFeign.select(attribute.getId(), page);


        model.addAttribute("orders",r);


        return "orderList";
    }

}
