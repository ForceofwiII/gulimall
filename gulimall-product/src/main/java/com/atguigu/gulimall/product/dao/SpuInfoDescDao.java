package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * spu信息介绍
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface SpuInfoDescDao extends BaseMapper<SpuInfoDescEntity> {


     @Select("select * from pms_spu_info_desc where spu_id= #{spuId}")
     SpuInfoDescEntity getBySpuId(Long spuId);
}
