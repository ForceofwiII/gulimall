package com.atguigu.gulimall.search.controller;


import com.atguigu.common.exception.BizCodeEnume;
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
@RequestMapping("search")
public class EsSaveController {

    @Autowired
    ProudctSaveService proudctSaveService;




    @PostMapping("/product")
    public R up(@RequestBody List<SkuEsModel> models)  {
    boolean  b=false;

   try{
        b= proudctSaveService.saveProduct(models);
   }
   catch (Exception e){


       return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());

   }

       if(b)
        return  R.ok();
       else {
           return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());

       }

    }

}
