package com.atguigu.gulimall.order.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class GulimallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {

        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        //放大作用域
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULISESSION");

        return cookieSerializer;
    }


    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {




        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.setSafeMode(false);  // 禁用 safeMode
        parserConfig.setAutoTypeSupport(true);  // 启用 autoType
        parserConfig.addAccept("com.atguigu.common.vo.MemberEntityVo");

        // 添加日志输出
        System.out.println("SafeMode: " + parserConfig.isSafeMode());
        System.out.println("AutoTypeSupport: " + parserConfig.isAutoTypeSupport());




       //  return new GenericJackson2JsonRedisSerializer();
       return new GenericFastJsonRedisSerializer();
    }

}
