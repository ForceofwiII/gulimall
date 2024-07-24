package com.atguigu.gulimall.product.dao;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * spu信息
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    @Update("update  pms_spu_info  set publish_status=#{status}, update_time=now() where id =#{id} ")
    void updateSpuStatus(@Param("id")Long id,@Param("status") int status);
}
