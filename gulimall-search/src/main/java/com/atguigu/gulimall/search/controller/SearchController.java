package com.atguigu.gulimall.search.controller;


import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;


    @GetMapping("/")
    public String indexPage(){
        return "list";
    }


    @GetMapping("/list.html") //按照条件检索
    public  String listpage(  SearchParam searchParam , Model model) throws IOException {

        SearchResult result = mallSearchService.search(searchParam);

        model.addAttribute("result",result);
        return "list";
    }
}
