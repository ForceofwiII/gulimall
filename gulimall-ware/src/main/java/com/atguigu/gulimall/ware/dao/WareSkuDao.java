package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品库存
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:59:40
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getStock(Long o);


    @Select("select * from wms_ware_sku where sku_id = #{skuId} and stock-wms_ware_sku.stock_locked >= #{num}")
    List<Long> hasStock(@Param("skuId") Long skuId ,@Param("num") Integer num);


    @Update("update wms_ware_sku set stock_locked = stock_locked + #{count} where ware_id = #{wareId} and sku_id = #{skuId} and stock - stock_locked >= #{count} ")
    int lockStock(@Param("wareId")  Long wareId, @Param("count") Integer count,@Param("skuId") Long skuId);


    @Update("update wms_ware_sku set stock_locked = stock_locked - #{skuNum}  where ware_id = #{wareId} and sku_id = #{skuId}")
    void unlockStock(@Param("wareId")  Long wareId, @Param("skuId")  Long skuId, @Param("skuNum")  Integer skuNum);
}
