package com.atguigu.gulimall.ware.listener;


import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.feign.OrderFeign;
import com.atguigu.gulimall.ware.vo.OrderEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service




public class UnlockStock {

    @Autowired
    OrderFeign orderFeign;

    @Autowired
    WareSkuDao wareSkuDao;





    @RabbitListener(queues = "stock.release.stock.queue")
    public void autoUnlockStock(Message message){

        StockLockedTo stockLockedTo  = JSON.parseObject(message.getBody(), StockLockedTo.class);
        System.out.println("收到消息"+stockLockedTo);

        //如果订单不存在或者已取消 要解锁库存
        List<StockDetailTo> detailTos = stockLockedTo.getDetailTos();

        OrderEntity order = orderFeign.getOrder(stockLockedTo.getOrderSn());
        if(order==null  || order.getStatus()==4){

            unlockStock(detailTos);

        }
        else{
            return;
        }






    }

    //解锁库存
    public void unlockStock(List<StockDetailTo> detailTos){


        for (StockDetailTo detailTo : detailTos) {

            Long wareId = detailTo.getWareId();
            Long skuId = detailTo.getSkuId();
            Integer skuNum = detailTo.getSkuNum();

            wareSkuDao.unlockStock(wareId,skuId,skuNum);


        }

    }


}
