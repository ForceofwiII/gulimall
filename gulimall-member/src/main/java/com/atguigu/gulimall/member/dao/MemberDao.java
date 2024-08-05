package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.vo.UserLoginVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:47:05
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {


    @Select("select * from ums_member where username = #{loginacct} or mobile = #{loginacct} and password = #{password}")
    MemberEntity login(UserLoginVo userloginVo);


    @Select("select * from ums_member where username = #{loginacct} or mobile = #{loginacct}")
    MemberEntity selectByAccount(UserLoginVo userloginVo);
}
