package com.atguigu.gulimall.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class GithubUser {

    private String login;


    private Long id;


    private String name;

    private String email;

    private String location;


}
