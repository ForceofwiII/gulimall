package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);


    @Select("select * from pms_attr_attrgroup_relation where attr_id = #{attrId}")
    AttrAttrgroupRelationEntity selectByAttrId(Long attrId);


    @Update("update pms_attr_attrgroup_relation set attr_group_id = #{attrGroupId} where attr_id = #{attrId}")
    void updateByAttrId(@Param("attrId")Long  attrId, @Param("attrGroupId") Long attrGroupId);

    @Select("select attr_id from pms_attr_attrgroup_relation where attr_group_id = #{attrgroupId}")
    List<Long> selectByGroupId(Long attrgroupId);
}
