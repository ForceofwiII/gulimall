package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @Description: 发送到mq消息队列的to
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-07-06 21:03
 **/

@Data
public class StockLockedTo {

    /** 库存工作单的id **/
    private Long id;

    //订单号
    private String orderSn;

    /** 工作单详情的所有信息 **/
    private List<StockDetailTo> detailTos;
}
