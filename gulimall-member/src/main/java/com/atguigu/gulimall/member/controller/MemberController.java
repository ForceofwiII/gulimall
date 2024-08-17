package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.vo.MemberEntityVo;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;
import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.feign.OrderFeign;
import com.atguigu.gulimall.member.vo.GithubUser;
import com.atguigu.gulimall.member.vo.GoogleUser;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.UserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.servlet.http.HttpSession;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;


/**
 * 会员
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:47:05
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    OrderFeign orderFeign;

//    @Autowired
//    CouponFeignService couponFeignService;

//    @RequestMapping("/coupons")
//    public R test(){
//        MemberEntity memberEntity = new MemberEntity();
//        memberEntity.setNickname("张三");
//
//     R membercoupons = couponFeignService.membercoupons();
//        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
//    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/regist")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo){
        try{
            memberService.register(memberRegisterVo);
            return R.ok();
        }
        catch (PhoneException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        catch (UsernameException u){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userloginVo){


         MemberEntity member=  memberService.login(userloginVo);
            if(member==null){
                return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());

            }

        return  R.ok().setData(member);

    }

    @PostMapping("/github/login")
    public R githubLogin(@RequestBody GithubUser githubUser){
        MemberEntity member=  memberService.githubLogin(githubUser);
        if(member==null){
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());

        }

        return  R.ok().setData(member);

    }

    @PostMapping("/google/login")
    public R googleLogin(@RequestBody GoogleUser googleUser){
        MemberEntity member=  memberService.googleLogin(googleUser);
        if(member==null){
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());

        }

        return  R.ok().setData(member);

    }

    @GetMapping("/{id}/getAddress")
    public List<MemberReceiveAddressEntity> getAddress(@PathVariable("id") Long id){
        List<MemberReceiveAddressEntity> address = memberService.getAddress(id);
        return address;
    }





}
