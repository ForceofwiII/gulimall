package com.atguigu.gulimall.auth.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")

public interface ThirdPart {
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String  phone, @RequestParam("code") String code);

}
