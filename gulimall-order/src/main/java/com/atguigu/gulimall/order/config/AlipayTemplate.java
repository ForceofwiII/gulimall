package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public String app_id="9021000140606025";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public String merchant_private_key="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDHG0hs7Gkj8+SsRyl5KtTgJQaZCjDmHATo5OFQxwNhR+Sx+8VCbJRlsggttzaDqHsjp3rwwj+63KfCaAgSf2clXoXwedqQ2PdYONZSA6rPz0I0kPsrP42X1kBfu6t6rrAVqGe7xeIf4H85JwcY3SF4aaEMMkzflKxiUJGVBjWOnXrZX2D8nJ/SL4AHzUlXThk33Vz3GG6s1zRxjnZgvJ2c3VcyqykTkWEnGzoDhIEOcDquA9Oc1FLqnBOrKxa1r/HN4zAXj7ycCciXwqZg73k53N6BRhm8s8dWAr0yCNxWld+g9YOqwYwvt25Q/yNz3M77fh4RDe7TGVHUSi0ZUNcDAgMBAAECggEAHd7R9sVg4KvuWooyhqMOXLIxUzRcMalycx4jT2ML9sQ6YxlKdrSjbr5Oy8z4uLfGAYtWvlfmB7qi0F+bFmtWphyQK4ucZq6mLO4Z+SPjtFl7rTcy90IJlAXoknQtPbKHaDhjl6AQVIfxjjl0rx49rzrpllLVhZVgZz9/F8RetI5Xztjz6rwb7pblp0SO9xVXI0TfNO/AtXgSCFTe0bpTRqfgUVqDWY3DntNftPZWSaMiCFnx10t+LoAqD5D1wnyoKzAGlxElSoFyP3HHplonl/jgWjqJzOVQcNbWSoZw8eqoUeFruHTk3DtmcGNGNvkuyK+il0oQ4fE/gsGE/BXrgQKBgQDuw1Y5dLi5aIbbC4OKkup8r5RgRnuDJyfYUQx3WvmgGv5VWgOpLPl9Nb41kD9QIAYddjg2R9zvt+9p6xrXSdpzE+0fQHNUdbusOufAGAA1Saw1+J+82gxnq6io4oaICeVF9Cegm0F1Oyd/EAb3TovhiDbUS8VPM/7435/gkxL62wKBgQDVewqKYIVd6AXaJk6VAy71QIaUSOgtastI8/XVMNmQiomhlHd34SYGMVZj17i2c7BsfUOJuC+xrMfviZ5gqM2CsCqCzb6PAa1jXp/5cewxwsTAMvapWJMHmlTk/buFcuxS8X9rp/Wj8dj6Z0B9HZ/ucXHnezdUh4J1mMdCYUwI+QKBgCJ259dbVd7Ni8nKm65BDr/4yD4tbl68JDBBYr9Hzoih3NyNJcIhz9GXFGG+9KSoOrm9+tc7AFVKA7ESUsmIy5OY6L1+HCew5uoxhopf7IfEnqsbvFMSVoAQc0QNjsXMR+AVIjM4tfYqF8VrltXT7LUdaGy0k0Kfisp53SYZlL/JAoGBALs1q4iuHEa9smQcM815fTSV4W8ogHNTs/HF/092B1U6KHgKOrpmN3I20gu8ob9etNhvAUtEuo6VdsAZb0pUveRdmOocg3/leKgr0x5tSTRaGTBzgNwBUO7Z6Jjn6gqdPaF/Jr7CDCg5bo4o/eS6lgYEdl8JLvxx2XZHBHax3HwBAoGBAMc6oDp03zAT5KAR5xNTOFNqK4+SnsqJ4g0PNqa18JSKzTQXhxo5ekZlpiWjxIYsMVPW/CyVc/dgBwAcRFopXv5EUfaHQZI4oN8lZi6Bz45ZCKx6tjlvK4heECs1G84qajAntZyfn+6wajziqEqBiKhg+18+yHRl6zexjsQBChbt";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public String alipay_public_key="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjp3GYagoxARti89bro3Xt/dirOeD2LQLMpjmOy5oO0u1VOeOuEsbsLc+cEEIcfK84WhPnB6ansiDhs7gHCZzF9JOc3+AoCo7ay0XCaFwsRfbw+FeZZXnZ8/+p0RvRRKM8GRqg3XANlXnHQxbHpaAcm8dQaAQHlChwO5nlv52UwJtl4GmtRB4szbG2E0a6wf/VGxZIGYY00HuQ81AykhNK+DcaL+4Rc+Jvv1db1U5ibvx95FnBZpS49EsF16l3A1HADLNTcULmJSDg/N6LNHZlwMXR2+oO5hzhk1t2lxJn7wBl/OLQE+HKssmdcfIGXQmQ0eZfeSo0AIJ+ogJy25BeQIDAQAB";

    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    public String notify_url="http://localhost:8020/order/pay/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    public String return_url="http://localhost:8020/order/pay/success";

    // 签名方式
    private  String sign_type="RSA2";

    // 字符编码格式
    private  String charset="utf-8";

    //订单超时时间
    private String timeout = "1m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    public String gatewayUrl="https://openapi-sandbox.dl.alipaydev.com/gateway.do" ;

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(vo.getOut_trade_no());
        model.setTotalAmount(vo.getTotal_amount());
        model.setSubject(vo.getSubject());
      //  model.setBody(vo.getBody());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
       // model.setTimeExpire(timeout);
        alipayRequest.setBizModel(model);

        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest, "POST");
//        alipayRequest.setReturnUrl(return_url);
//        alipayRequest.setNotifyUrl(notify_url);
//
//        //商户订单号，商户网站订单系统中唯一订单号，必填
//        String out_trade_no = vo.getOut_trade_no();
//        //付款金额，必填
//        String total_amount = vo.getTotal_amount();
//        //订单名称，必填
//        String subject = vo.getSubject();
//        //商品描述，可空
//        String body = vo.getBody();
//
//        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
//                + "\"total_amount\":\""+ total_amount +"\","
//                + "\"subject\":\""+ subject +"\","
//                + "\"body\":\""+ body +"\","
//                + "\"timeout_express\":\""+timeout+"\","
//                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
//
//        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);




       // String result =      "https://openapi-sandbox.dl.alipaydev.com/gateway.do"+"?"+response.getBody();
        String result = response.getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
