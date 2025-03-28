package com.atguigu.gulimall.product.web;


import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {


    @Autowired
    CategoryService categoryService;


    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        //查出所有的1级分类

      List<CategoryEntity> categoryEntityList=      categoryService.getLevel1();

      model.addAttribute("categories",categoryEntityList);



        //返回首頁
        return "index";
    }



    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String,List<Catelog2Vo>> getCataLogJson(){
        //1、查出所有的1级分类


        return categoryService.getCataLogJson();


    }



}
