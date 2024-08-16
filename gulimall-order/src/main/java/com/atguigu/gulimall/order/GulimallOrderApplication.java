package com.atguigu.gulimall.order;

import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GulimallOrderApplication {

    public static void main(String[] args) {



        ParserConfig.getGlobalInstance().setSafeMode(false);
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);  // 启用 autoType
        ParserConfig.getGlobalInstance().addAccept("com.atguigu.common.vo.MemberEntityVo");
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
