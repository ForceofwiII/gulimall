package com.atguigu.gulimall.ware.listener;


import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.dao.WareOrderTaskDao;
import com.atguigu.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
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

    @Autowired
    WareOrderTaskDao wareOrderTaskDao;

    @Autowired
    WareOrderTaskDetailDao wareOrderTaskDetailDao;





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


    @RabbitHandler
    public void autoUnlockStock2(OrderEntity order){

         System.out.println("收到消息"+order);
        String orderSn = order.getOrderSn();
          Long id   =  wareOrderTaskDao.getByOrderSn(orderSn);
          List<WareOrderTaskDetailEntity>  wareOrderTaskDetailEntities = wareOrderTaskDetailDao.selectByTaskId(id);
          unlockStock2(wareOrderTaskDetailEntities);


    }

    //解锁库存
    public void unlockStock(List<StockDetailTo> detailTos){


        for (StockDetailTo detailTo : detailTos) {


            WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailDao.selectById(detailTo.getTaskDetailId());
            if(wareOrderTaskDetailEntity.getLockStatus()==2){
                continue;  //已经解锁过了
            }

            Long wareId = detailTo.getWareId();
            Long skuId = detailTo.getSkuId();
            Integer skuNum = detailTo.getSkuNum();

            wareSkuDao.unlockStock(wareId,skuId,skuNum);

            wareOrderTaskDetailDao.updateDetailStatus(detailTo.getTaskDetailId(),2);


        }

    }


    public  void unlockStock2( List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities){


        for (WareOrderTaskDetailEntity wareOrderTaskDetailEntity : wareOrderTaskDetailEntities) {


            if(wareOrderTaskDetailEntity.getLockStatus()==2){
                continue;  //已经解锁过了
            }

            Long wareId = wareOrderTaskDetailEntity.getWareId();
            Long skuId = wareOrderTaskDetailEntity.getSkuId();
            Integer skuNum = wareOrderTaskDetailEntity.getSkuNum();
            wareSkuDao.unlockStock(wareId,skuId,skuNum);

            //更新工作单的状态
            wareOrderTaskDetailDao.updateDetailStatus(wareOrderTaskDetailEntity.getId(),2);



        }

    }


}
