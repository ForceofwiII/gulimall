package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProudctSaveService {



    void saveProduct(List<SkuEsModel> models) throws IOException;
}
