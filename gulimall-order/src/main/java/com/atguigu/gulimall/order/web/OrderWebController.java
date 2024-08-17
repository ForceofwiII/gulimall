package com.atguigu.gulimall.order.web;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;


    @Autowired
    RedissonClient redissonClient;

    @Autowired
    OrderItemService orderItemService;



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


    @PostMapping("/select/{memberId}")
    @ResponseBody
    public R select(@PathVariable("memberId") Long memberId , @RequestBody Map<String,Object> params){
        IPage<OrderEntity> page = orderService.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>().eq("member_id", memberId));
        List<OrderEntity> orderSn = page.getRecords().stream().map((o) -> {

            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", o.getOrderSn()));
            o.setOrderItemEntityList(orderItemEntities);

            return o;


        }).collect(Collectors.toList());

        page.setRecords(orderSn);


        return R.ok().put("page", new PageUtils(page));


    }


}
