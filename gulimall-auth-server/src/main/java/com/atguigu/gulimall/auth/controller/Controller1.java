package com.atguigu.gulimall.auth.controller;


import com.atguigu.common.constant.AuthServerConstant;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class Controller1 {

    @GetMapping({"/"})
    public String login(){
        return "login";
    }

    @GetMapping("/reg.html")
    public String reg(){
        return "reg";
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){

        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null){
            return "redirect:http://gulimall.com";
        }


        return "login";
    }
}
