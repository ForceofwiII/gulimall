package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProudctSaveService;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class ProductSaveServiceImpl implements ProudctSaveService {

   @Autowired
    RestHighLevelClient restHighLevelClient;


    @Override
    public boolean saveProduct(List<SkuEsModel> models) throws IOException {

        //新建一个索引 用kibana

       //向es中保存数据

        BulkRequest bulkRequest = new BulkRequest();


        for (SkuEsModel model : models) {

            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);

            indexRequest.id(model.getSkuId().toString());
            String jsonString = JSON.toJSONString(model);
            indexRequest.source(jsonString, XContentType.JSON);



            bulkRequest.add(indexRequest);

        }


        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //todo 判断是否有错误

        boolean b = bulk.hasFailures();



        return !b;



    }
}
