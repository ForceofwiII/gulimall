package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 库存工作单
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:59:40
 */
@Mapper
public interface WareOrderTaskDetailDao extends BaseMapper<WareOrderTaskDetailEntity> {


    @Select("select * from wms_ware_order_task_detail where task_id=#{id}")
    List<WareOrderTaskDetailEntity> selectByTaskId(Long id);


    @Update("update wms_ware_order_task_detail set lock_status=#{i} where id=#{id}")
    void updateDetailStatus(@Param("id") Long id, @Param("i")   int i);
}
