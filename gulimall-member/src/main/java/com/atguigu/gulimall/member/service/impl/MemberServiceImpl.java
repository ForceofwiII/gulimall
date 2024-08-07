package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.vo.GithubUser;
import com.atguigu.gulimall.member.vo.GoogleUser;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.UserLoginVo;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        memberEntity.setNickname(memberRegisterVo.getUserName());
        this.save(memberEntity);

    }

    @Override
    public MemberEntity login(UserLoginVo userloginVo) {

        String password = userloginVo.getPassword();


        MemberEntity memberEntity =   memberDao.selectByAccount(userloginVo);
        if(memberEntity==null){
            return null;
        }

        String password1 = memberEntity.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


        boolean matches = bCryptPasswordEncoder.matches(password, password1);
        if(!matches){
            return null;
        }


        return  memberEntity;
    }

    @Override
    public MemberEntity githubLogin(GithubUser githubUser) {
        String socialId = githubUser.getId().toString();
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_id", socialId));
        if(memberEntity==null){
            memberEntity = new MemberEntity();
            memberEntity.setSocialId(socialId);
            memberEntity.setNickname(githubUser.getName());

            memberEntity.setEmail(githubUser.getEmail());
            memberEntity.setCity(githubUser.getLocation());
            memberEntity.setLevelId(memberLevelDao.getDefaultLevelId());
            this.save(memberEntity);
        }

        return memberEntity;
    }

    @Override
    public MemberEntity googleLogin(GoogleUser googleUser) {
        String socialId = googleUser.getId();
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_id", socialId));
        if(memberEntity==null){
            memberEntity = new MemberEntity();
            memberEntity.setSocialId(socialId);
            memberEntity.setNickname(googleUser.getName());

            memberEntity.setEmail(googleUser.getEmail());

            memberEntity.setLevelId(memberLevelDao.getDefaultLevelId());
            this.save(memberEntity);
        }


        return memberEntity;
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