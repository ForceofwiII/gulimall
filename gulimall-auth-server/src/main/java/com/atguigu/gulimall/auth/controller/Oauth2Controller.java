package com.atguigu.gulimall.auth.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.GithubUser;
import com.atguigu.gulimall.auth.vo.GoogleUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@Slf4j
public class Oauth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;


    @GetMapping("/oauth2/github/success")
    public String github(@RequestParam("code") String code , HttpSession session) {
        //根据授权码获得令牌

        //糊涂工具包发送post请求
        HttpRequest form = HttpUtil.createPost("https://github.com/login/oauth/access_token")
                .form("client_id", "Ov23li8FGPVpKURum0hL")
                .form("client_secret", "1c3f91294dcb89f23b3a0faee1130826a426522e")
                .form("code", code)
                .form("redirect_uri", "http://auth.gulimall.com/oauth2/github/success");

        HttpResponse response = form.execute();
        if(response.getStatus()!=200){
            return "redirect:http://auth.gulimall.com/login.html";
        }
        String result = response.body();
        System.out.println(result);
        // 解析github响应
        String accessToken = null;
        String tokenType = null;
        String scope = null;

        String[] pairs = result.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                switch (keyValue[0]) {
                    case "access_token":
                        accessToken = keyValue[1];
                        break;
                    case "token_type":
                        tokenType = keyValue[1];
                        break;
                    case "scope":
                        scope = keyValue[1];
                        break;
                }
            }
        }

        //根据令牌获得用户信息

        HttpResponse response2 = HttpRequest.get("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .execute();

        // 打印原始响应
        String responseBody = response2.body();
        System.out.println("Raw Response: " + responseBody);

        // 解析响应
        GithubUser githubUser = JSON.parseObject(responseBody, GithubUser.class);
        System.out.println( githubUser);

        //如果用户是第一次进来，就注册一个账号

        R r = memberFeignService.githubLogin(githubUser);
        if(r.getCode()!=0){
            throw new RuntimeException("登录失败");
        }

        MemberEntityVo data = r.getData(new TypeReference<MemberEntityVo>() {
        });
        log.info("登录成功：用户信息：{}",data);
        session.setAttribute("loginUser",data);


        return "redirect:http://gulimall.com";


    }

    @GetMapping("/oauth2/google/success")
    public String google(@RequestParam("code") String code , HttpSession session) {
        //根据授权码获得令牌

        //糊涂工具包发送post请求


            String clientId = "277198482730-6pa51ek3fipd5uengfljivlko8dqein2.apps.googleusercontent.com";
            String clientSecret = "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";
            String redirectUri = "http://auth.gulimall.com/oauth2/google/success";

            HttpResponse response = HttpRequest.post("https://oauth2.googleapis.com/token")
                    .form("code", code)
                    .form("client_id", clientId)
                    .form("client_secret", clientSecret)
                    .form("redirect_uri", redirectUri)
                    .form("grant_type", "authorization_code")
                    .execute();

            String body = response.body();
            System.out.println(body);

        Map map = JSON.parseObject(body, Map.class);
        String accessToken = (String) map.get("access_token");

        //根据令牌获得用户信息
        HttpResponse response1 = HttpRequest.get("https://www.googleapis.com/oauth2/v1/userinfo")
                .form("access_token", accessToken)
                .execute();

        String userInfo = response1.body();
        System.out.println("User Info: " + userInfo);
        GoogleUser googleUser = JSON.parseObject(userInfo, GoogleUser.class);
        R r = memberFeignService.googleLogin(googleUser);
        if(r.getCode()!=0){
            throw new RuntimeException("登录失败");
        }

        MemberEntityVo data = r.getData(new TypeReference<MemberEntityVo>() {
        });
        log.info("登录成功：用户信息：{}",data);

        session.setAttribute("loginUser",data);



        return "redirect:http://gulimall.com";



    }
}
