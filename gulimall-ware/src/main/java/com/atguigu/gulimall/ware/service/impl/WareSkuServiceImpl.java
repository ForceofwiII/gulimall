package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareOrderTaskDao;
import com.atguigu.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeign;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.vo.*;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Autowired
    WareOrderTaskDao wareOrderTaskDao;

    @Autowired
    WareOrderTaskDetailDao wareOrderTaskDetailDao;

    @Autowired
    OrderFeign orderFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
           // 1、自己catch异常
           // TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");


                    skuEntity.setSkuName((String) data.get("skuName"));

            }catch (Exception e){

            }


            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> hasstock(List<Long> ids) {


        List<SkuHasStockVo> collect = ids.stream().map(o -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();

           Long stock= this.baseMapper.getStock(o);
           if(stock==null)
               stock=0L;

           skuHasStockVo.setSkuId(o);

           skuHasStockVo.setHasStock(stock>0);

            return skuHasStockVo;
        }).collect(Collectors.toList());

        return  collect;

    }

    @Override
    public Map<Long, Boolean> listHasStock(List<Long> skuIds) {

        Map<Long,Boolean> map = new HashMap<>();
        if(skuIds != null && skuIds.size() > 0){
            List<SkuHasStockVo> vos = hasstock(skuIds);
            for (SkuHasStockVo vo : vos) {
                map.put(vo.getSkuId(),vo.getHasStock());
            }
        }
        return map;
    }

    @Override  //锁定库存
    @Transactional(rollbackFor = Exception.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        Boolean locked = false;

        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskDao.insert(wareOrderTaskEntity);
        List<WareOrderTaskDetailEntity> list = new ArrayList<>();

        List<OrderItemVo> locks = vo.getLocks();
        List<LockStockResultVo> collect = locks.stream().map((o) -> {
            LockStockResultVo resultVo = new LockStockResultVo();
            resultVo.setSkuId(o.getSkuId());
            //查询所有有库存的仓库
            List<Long> ids = wareSkuDao.hasStock(o.getSkuId(), o.getCount());
            if (ids == null || ids.size() == 0) {
                throw new RuntimeException(o.getTitle() + "库存不足");
            }
            for (Long id : ids) {
                int count=   wareSkuDao.lockStock(id, o.getCount(), o.getSkuId());
                if(count==0) {
                    resultVo.setLocked(false);

                }
                else {
                    resultVo.setLocked(true);

                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setSkuId(o.getSkuId());
                    wareOrderTaskDetailEntity.setSkuName(o.getTitle());
                    wareOrderTaskDetailEntity.setSkuNum(o.getCount());
                    wareOrderTaskDetailEntity.setWareId(id);
                    wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    wareOrderTaskDetailDao.insert(wareOrderTaskDetailEntity);
                    list.add(wareOrderTaskDetailEntity);

                    break;
                }

            }
            if(resultVo.getLocked()==false){
                throw new RuntimeException(o.getTitle() + "库存不足");
            }

            resultVo.setNum(o.getCount());


            return resultVo;
        }).collect(Collectors.toList());


        //todo 所有订单项都锁定库存了发送延迟消息来自动解锁库存
        StockLockedTo stockLockedTo = new StockLockedTo();
        stockLockedTo.setId(wareOrderTaskEntity.getId());
        List<StockDetailTo> tos = list.stream().map((o) -> {
            StockDetailTo stockDetailTo = new StockDetailTo();
            BeanUtils.copyProperties(o, stockDetailTo);
            stockDetailTo.setTaskDetailId(o.getId());
            return stockDetailTo;
        }).collect(Collectors.toList());

        stockLockedTo.setDetailTos(tos);
        stockLockedTo.setOrderSn(vo.getOrderSn());
        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.release.locked", stockLockedTo, message -> {
            message.getMessageProperties().setDelay(600000); //延迟10分钟
            return message;
        });

        return true;
    }






}