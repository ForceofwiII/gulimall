package com.atguigu.gulimall.auth.vo;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GoogleUser {

    private String id;
    private String email;
    private  String name;

}
