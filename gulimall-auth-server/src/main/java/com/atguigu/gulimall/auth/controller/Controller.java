package com.atguigu.gulimall.auth.controller;


import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class Controller {

    @GetMapping({"login.html","/"})
    public String login(){
        return "login";
    }

    @GetMapping("/reg.html")
    public String reg(){
        return "reg";
    }
}
