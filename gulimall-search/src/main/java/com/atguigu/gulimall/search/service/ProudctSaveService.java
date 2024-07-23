package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProudctSaveService {



    boolean saveProduct(List<SkuEsModel> models) throws IOException;
}
