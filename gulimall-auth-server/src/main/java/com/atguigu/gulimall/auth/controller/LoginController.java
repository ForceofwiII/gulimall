package com.atguigu.gulimall.auth.controller;


import cn.hutool.Hutool;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPart;
import com.atguigu.gulimall.auth.vo.MemberRegisterVo;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPart thirdPart;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;



    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        if(StringUtils.isEmpty(phone)){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }


        //1.接口防刷

        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(s)){

            return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
        }


        String code = RandomUtil.randomNumbers(6);//随机生成6位验证码
        //把验证码存到redis

        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,60, TimeUnit.SECONDS);

        //远程调用第三方微服务发送验证码
      //  thirdPart.sendCode(phone,code);

        System.out.println("success");


        return R.ok();
    }



    @PostMapping("/regist")

    public String regist(@Valid  UserRegisterVo  userRegisterVo, BindingResult result, RedirectAttributes redirectAttributes){

          if(result.hasErrors()){
              //如果数据不合法

              System.out.println("数据校验出现问题");
              Map<String, String> errors = result.getFieldErrors().stream().collect(
                      Collectors.toMap(
                              e -> e.getField(),
                              e -> e.getDefaultMessage(),
                              (existing, replacement) -> existing // 保留现有值
                      )
              );



              //重定向携带数据 把数据放到session中,一次后就消失 todo 解决分布式session问题
              redirectAttributes.addFlashAttribute("errors",errors);

              return "redirect:http://auth.gulimall.com/reg.html";
        }

      //校验验证码
        String code = userRegisterVo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if(StringUtils.isEmpty(s)){
            Map<String,String> errors =  new HashMap<>();
            errors.put("code","验证码已过期");

            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        if(!code.equals(s)){
            Map<String,String> errors =  new HashMap<>();
            errors.put("code","验证码错误,请重新输入");
            

            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }


        stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX+userRegisterVo.getPhone());


        //调用远程服务进行注册
        MemberRegisterVo memberRegisterVo = new MemberRegisterVo();
        BeanUtils.copyProperties(userRegisterVo,memberRegisterVo);
        R r = memberFeignService.register(memberRegisterVo);

        if(r.getCode()!=0){
            Map<String,String> errors =  new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));


            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }





        return "redirect:http://auth.gulimall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo loginVo, RedirectAttributes redirectAttributes, HttpSession session){


        R login = memberFeignService.login(loginVo);
        if(login.getCode()!=0){
            Map<String,String> errors =  new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));


            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

        session.setAttribute(AuthServerConstant.LOGIN_USER,login.getData(new TypeReference<MemberEntityVo>(){}));



        return "redirect:http://gulimall.com";

    }

    @GetMapping("/loguot.html")
    public String logout(HttpSession httpSession){

        System.out.println("hello");

        httpSession.invalidate();

        return "redirect:http://gulimall.com";
    }

}
