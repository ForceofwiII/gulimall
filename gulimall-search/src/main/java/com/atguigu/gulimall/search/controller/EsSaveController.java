package com.atguigu.gulimall.search.controller;


import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProudctSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class EsSaveController {

    @Autowired
    ProudctSaveService proudctSaveService;

    @RequestMapping("/search")


    @PostMapping("/product")
    public R up(@RequestBody List<SkuEsModel> models) throws IOException {



       proudctSaveService.saveProduct(models);

        return  R.ok();

    }

}
