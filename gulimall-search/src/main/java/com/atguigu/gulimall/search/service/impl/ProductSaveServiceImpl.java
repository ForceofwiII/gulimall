package com.atguigu.gulimall.search.service.impl;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.service.ProudctSaveService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class ProductSaveServiceImpl implements ProudctSaveService {

   @Autowired
    RestHighLevelClient restHighLevelClient;


    @Override
    public void saveProduct(List<SkuEsModel> models) throws IOException {

        //新建一个索引 用kibana

       //向es中保存数据

        BulkRequest bulkRequest = new BulkRequest();





        restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);


    }
}
