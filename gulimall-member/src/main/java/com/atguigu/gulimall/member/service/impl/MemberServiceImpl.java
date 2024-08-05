package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Autowired
    MemberDao memberDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo memberRegisterVo) {

        MemberEntity memberEntity = new MemberEntity();

        //设置默认等级
        memberEntity.setLevelId(memberLevelDao.getDefaultLevelId());

        //检测用户名和手机号是否唯一
        checkUsernameUnique(memberRegisterVo.getUserName());
        checkPhoneUnique(memberRegisterVo.getPhone());

        //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegisterVo.getPassword());
        memberEntity.setPassword(encode);






        memberEntity.setUsername(memberRegisterVo.getUserName());
        memberEntity.setMobile(memberRegisterVo.getPhone());
        this.save(memberEntity);

    }


    void checkUsernameUnique(String username){


        Long username1 = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(username1>0){
            throw  new   UsernameException();
        }
    }

    void checkPhoneUnique(String phone){
        Long phone1 = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(phone1>0){
            throw new PhoneException();
        }
    }

}