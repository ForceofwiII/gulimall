package com.atguigu.gulimall.cart.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Controller1 {

    @GetMapping({"/"})
    public String cart(){
        return "success";
    }
}
