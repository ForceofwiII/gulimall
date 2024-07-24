package com.atguigu.gulimall.product.web;


import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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


}
