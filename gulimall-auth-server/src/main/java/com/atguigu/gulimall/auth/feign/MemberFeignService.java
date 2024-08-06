package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo);


    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo userloginVo);

    @PostMapping("/member/member/github/login")
    public R githubLogin(@RequestBody GithubUser githubUser);

    @PostMapping("/member/member/google/login")
    public R googleLogin(@RequestBody GoogleUser googleUser);

}
