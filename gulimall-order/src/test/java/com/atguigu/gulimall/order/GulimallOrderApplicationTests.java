package com.atguigu.gulimall.order;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    OrderService orderService;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    AlipayTemplate alipayTemplate;

    @Test
    public void contextLoads() {


        DirectExchange directExchange = new DirectExchange("hello.direct", true, false);


        amqpAdmin.declareExchange(directExchange);


        Queue queue = new Queue("hello.queue", true, false, false);

        amqpAdmin.declareQueue(queue);


        amqpAdmin.declareBinding(new Binding("hello.queue", Binding.DestinationType.QUEUE, "hello.direct", "hello", null));


    }


    @Test
    public void testSend() {

        OrderEntity order = new OrderEntity();
        order.setOrderSn("123456");
        order.setId(1L);
        order.setBillContent("测试订单");
        rabbitTemplate.convertAndSend("hello.direct", "hello", "hello");


    }


    @Test
    public void test() throws AlipayApiException {


          // System.out.println(diagnosisUrl);
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no("2020080100012");
        payVo.setSubject("test");
        payVo.setTotal_amount("88");
        payVo.setBody("test");
        String pay = alipayTemplate.pay(payVo);

        System.out.println(pay);


    }

    private static AlipayConfig getAlipayConfig() {
        String privateKey  = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDHG0hs7Gkj8+SsRyl5KtTgJQaZCjDmHATo5OFQxwNhR+Sx+8VCbJRlsggttzaDqHsjp3rwwj+63KfCaAgSf2clXoXwedqQ2PdYONZSA6rPz0I0kPsrP42X1kBfu6t6rrAVqGe7xeIf4H85JwcY3SF4aaEMMkzflKxiUJGVBjWOnXrZX2D8nJ/SL4AHzUlXThk33Vz3GG6s1zRxjnZgvJ2c3VcyqykTkWEnGzoDhIEOcDquA9Oc1FLqnBOrKxa1r/HN4zAXj7ycCciXwqZg73k53N6BRhm8s8dWAr0yCNxWld+g9YOqwYwvt25Q/yNz3M77fh4RDe7TGVHUSi0ZUNcDAgMBAAECggEAHd7R9sVg4KvuWooyhqMOXLIxUzRcMalycx4jT2ML9sQ6YxlKdrSjbr5Oy8z4uLfGAYtWvlfmB7qi0F+bFmtWphyQK4ucZq6mLO4Z+SPjtFl7rTcy90IJlAXoknQtPbKHaDhjl6AQVIfxjjl0rx49rzrpllLVhZVgZz9/F8RetI5Xztjz6rwb7pblp0SO9xVXI0TfNO/AtXgSCFTe0bpTRqfgUVqDWY3DntNftPZWSaMiCFnx10t+LoAqD5D1wnyoKzAGlxElSoFyP3HHplonl/jgWjqJzOVQcNbWSoZw8eqoUeFruHTk3DtmcGNGNvkuyK+il0oQ4fE/gsGE/BXrgQKBgQDuw1Y5dLi5aIbbC4OKkup8r5RgRnuDJyfYUQx3WvmgGv5VWgOpLPl9Nb41kD9QIAYddjg2R9zvt+9p6xrXSdpzE+0fQHNUdbusOufAGAA1Saw1+J+82gxnq6io4oaICeVF9Cegm0F1Oyd/EAb3TovhiDbUS8VPM/7435/gkxL62wKBgQDVewqKYIVd6AXaJk6VAy71QIaUSOgtastI8/XVMNmQiomhlHd34SYGMVZj17i2c7BsfUOJuC+xrMfviZ5gqM2CsCqCzb6PAa1jXp/5cewxwsTAMvapWJMHmlTk/buFcuxS8X9rp/Wj8dj6Z0B9HZ/ucXHnezdUh4J1mMdCYUwI+QKBgCJ259dbVd7Ni8nKm65BDr/4yD4tbl68JDBBYr9Hzoih3NyNJcIhz9GXFGG+9KSoOrm9+tc7AFVKA7ESUsmIy5OY6L1+HCew5uoxhopf7IfEnqsbvFMSVoAQc0QNjsXMR+AVIjM4tfYqF8VrltXT7LUdaGy0k0Kfisp53SYZlL/JAoGBALs1q4iuHEa9smQcM815fTSV4W8ogHNTs/HF/092B1U6KHgKOrpmN3I20gu8ob9etNhvAUtEuo6VdsAZb0pUveRdmOocg3/leKgr0x5tSTRaGTBzgNwBUO7Z6Jjn6gqdPaF/Jr7CDCg5bo4o/eS6lgYEdl8JLvxx2XZHBHax3HwBAoGBAMc6oDp03zAT5KAR5xNTOFNqK4+SnsqJ4g0PNqa18JSKzTQXhxo5ekZlpiWjxIYsMVPW/CyVc/dgBwAcRFopXv5EUfaHQZI4oN8lZi6Bz45ZCKx6tjlvK4heECs1G84qajAntZyfn+6wajziqEqBiKhg+18+yHRl6zexjsQBChbt";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjp3GYagoxARti89bro3Xt/dirOeD2LQLMpjmOy5oO0u1VOeOuEsbsLc+cEEIcfK84WhPnB6ansiDhs7gHCZzF9JOc3+AoCo7ay0XCaFwsRfbw+FeZZXnZ8/+p0RvRRKM8GRqg3XANlXnHQxbHpaAcm8dQaAQHlChwO5nlv52UwJtl4GmtRB4szbG2E0a6wf/VGxZIGYY00HuQ81AykhNK+DcaL+4Rc+Jvv1db1U5ibvx95FnBZpS49EsF16l3A1HADLNTcULmJSDg/N6LNHZlwMXR2+oO5hzhk1t2lxJn7wBl/OLQE+HKssmdcfIGXQmQ0eZfeSo0AIJ+ogJy25BeQIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        alipayConfig.setAppId("9021000140606025");
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }


    @Test
    public void test2() throws AlipayApiException {
        // 沙箱环境网关
        String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        String appId = "9021000140606025";
        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDHG0hs7Gkj8+SsRyl5KtTgJQaZCjDmHATo5OFQxwNhR+Sx+8VCbJRlsggttzaDqHsjp3rwwj+63KfCaAgSf2clXoXwedqQ2PdYONZSA6rPz0I0kPsrP42X1kBfu6t6rrAVqGe7xeIf4H85JwcY3SF4aaEMMkzflKxiUJGVBjWOnXrZX2D8nJ/SL4AHzUlXThk33Vz3GG6s1zRxjnZgvJ2c3VcyqykTkWEnGzoDhIEOcDquA9Oc1FLqnBOrKxa1r/HN4zAXj7ycCciXwqZg73k53N6BRhm8s8dWAr0yCNxWld+g9YOqwYwvt25Q/yNz3M77fh4RDe7TGVHUSi0ZUNcDAgMBAAECggEAHd7R9sVg4KvuWooyhqMOXLIxUzRcMalycx4jT2ML9sQ6YxlKdrSjbr5Oy8z4uLfGAYtWvlfmB7qi0F+bFmtWphyQK4ucZq6mLO4Z+SPjtFl7rTcy90IJlAXoknQtPbKHaDhjl6AQVIfxjjl0rx49rzrpllLVhZVgZz9/F8RetI5Xztjz6rwb7pblp0SO9xVXI0TfNO/AtXgSCFTe0bpTRqfgUVqDWY3DntNftPZWSaMiCFnx10t+LoAqD5D1wnyoKzAGlxElSoFyP3HHplonl/jgWjqJzOVQcNbWSoZw8eqoUeFruHTk3DtmcGNGNvkuyK+il0oQ4fE/gsGE/BXrgQKBgQDuw1Y5dLi5aIbbC4OKkup8r5RgRnuDJyfYUQx3WvmgGv5VWgOpLPl9Nb41kD9QIAYddjg2R9zvt+9p6xrXSdpzE+0fQHNUdbusOufAGAA1Saw1+J+82gxnq6io4oaICeVF9Cegm0F1Oyd/EAb3TovhiDbUS8VPM/7435/gkxL62wKBgQDVewqKYIVd6AXaJk6VAy71QIaUSOgtastI8/XVMNmQiomhlHd34SYGMVZj17i2c7BsfUOJuC+xrMfviZ5gqM2CsCqCzb6PAa1jXp/5cewxwsTAMvapWJMHmlTk/buFcuxS8X9rp/Wj8dj6Z0B9HZ/ucXHnezdUh4J1mMdCYUwI+QKBgCJ259dbVd7Ni8nKm65BDr/4yD4tbl68JDBBYr9Hzoih3NyNJcIhz9GXFGG+9KSoOrm9+tc7AFVKA7ESUsmIy5OY6L1+HCew5uoxhopf7IfEnqsbvFMSVoAQc0QNjsXMR+AVIjM4tfYqF8VrltXT7LUdaGy0k0Kfisp53SYZlL/JAoGBALs1q4iuHEa9smQcM815fTSV4W8ogHNTs/HF/092B1U6KHgKOrpmN3I20gu8ob9etNhvAUtEuo6VdsAZb0pUveRdmOocg3/leKgr0x5tSTRaGTBzgNwBUO7Z6Jjn6gqdPaF/Jr7CDCg5bo4o/eS6lgYEdl8JLvxx2XZHBHax3HwBAoGBAMc6oDp03zAT5KAR5xNTOFNqK4+SnsqJ4g0PNqa18JSKzTQXhxo5ekZlpiWjxIYsMVPW/CyVc/dgBwAcRFopXv5EUfaHQZI4oN8lZi6Bz45ZCKx6tjlvK4heECs1G84qajAntZyfn+6wajziqEqBiKhg+18+yHRl6zexjsQBChbt";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjp3GYagoxARti89bro3Xt/dirOeD2LQLMpjmOy5oO0u1VOeOuEsbsLc+cEEIcfK84WhPnB6ansiDhs7gHCZzF9JOc3+AoCo7ay0XCaFwsRfbw+FeZZXnZ8/+p0RvRRKM8GRqg3XANlXnHQxbHpaAcm8dQaAQHlChwO5nlv52UwJtl4GmtRB4szbG2E0a6wf/VGxZIGYY00HuQ81AykhNK+DcaL+4Rc+Jvv1db1U5ibvx95FnBZpS49EsF16l3A1HADLNTcULmJSDg/N6LNHZlwMXR2+oO5hzhk1t2lxJn7wBl/OLQE+HKssmdcfIGXQmQ0eZfeSo0AIJ+ogJy25BeQIDAQAB";

        // 创建客户端
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey, "json", "UTF-8", alipayPublicKey, "RSA2");

        // 创建支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl("http://你的回调地址");
        request.setNotifyUrl("http://你的通知地址");

        // 填充业务参数
        request.setBizContent("{" +
                "\"out_trade_no\":\"202008010001\"," + // 商户订单号
                "\"total_amount\":\"88.88\"," +        // 支付金额
                "\"subject\":\"测试支付\"," +           // 商品标题
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                "}");

        // 执行请求
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功，返回URL：" + response.getBody());
        } else {
            System.out.println("调用失败");
        }
    }
    }




