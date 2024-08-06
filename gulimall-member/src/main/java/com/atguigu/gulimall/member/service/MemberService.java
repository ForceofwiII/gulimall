package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.vo.GithubUser;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.UserLoginVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:47:05
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo memberRegisterVo);

    MemberEntity login(UserLoginVo userloginVo);

    MemberEntity githubLogin(GithubUser githubUser);
}

