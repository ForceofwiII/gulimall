package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MallSearchServiceImpl  implements MallSearchService {


    @Autowired
    RestHighLevelClient client;


    @Override
    public SearchResult search(SearchParam searchParam) {





    }
}
