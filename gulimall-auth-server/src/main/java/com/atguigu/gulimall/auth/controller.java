package com.atguigu.gulimall.auth;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class controller {

    @GetMapping({"login.html","/"})
    public String login(){
        return "login";
    }

    @GetMapping("/reg.html")
    public String reg(){
        return "reg";
    }
}
