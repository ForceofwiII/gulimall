package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDescDao;
import com.atguigu.gulimall.product.entity.*;
//import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.EsFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired

    SpuInfoDescDao spuInfoDescDao;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    EsFeignService esFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * //TODO 高级部分完善
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);



        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(),images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);


//        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
//        Bounds bounds = vo.getBounds();
//        SpuBoundTo spuBoundTo = new SpuBoundTo();
//        BeanUtils.copyProperties(bounds,spuBoundTo);
//        spuBoundTo.setSpuId(infoEntity.getId());
//        R r = couponFeignService.saveSpuBounds(spuBoundTo);
//        if(r.getCode() != 0){
//            log.error("远程保存spu积分信息失败");
//        }


        //5、保存当前spu对应的所有sku信息；

        List<Skus> skus = vo.getSkus();
        if(skus!=null && skus.size()>0){
            skus.forEach(item->{
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

//                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
//                SkuReductionTo skuReductionTo = new SkuReductionTo();
//                BeanUtils.copyProperties(item,skuReductionTo);
//                skuReductionTo.setSkuId(skuId);
//                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
//                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
//                    if(r1.getCode() != 0){
//                        log.error("远程保存sku优惠信息失败");
//                    }
//                }



            });
        }






    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfos(SpuSaveVo vo) {
        //保存spu基本信息  spuinfo

        System.out.println(vo.getBaseAttrs().toString());

        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfos(spuInfoEntity);


        //保存spu图片描述    infodesc

        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        this.saveSpuInfoDecr(spuInfoDescEntity);



        //保存spu图片集   images

        


        List<String> images = vo.getImages();
        imagesService.saveImages(spuInfoEntity.getId(),images);



        //保存spu基本属性  attrvalue


        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((o) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(o.getAttrId());
            AttrEntity byId = attrService.getById(o.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            productAttrValueEntity.setAttrValue(o.getAttrValues());
            productAttrValueEntity.setQuickShow(o.getShowDesc());

            return productAttrValueEntity;


        }).collect(Collectors.toList());

        attrValueService.saveBatch(collect);



        //保存spu的积分信息  远程调用
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveSpuBounds(spuBoundTo);


        //保存spu的sku信息

        //5.1 sku基本信息

        List<Skus> skus = vo.getSkus();
        if(skus !=null || skus.size()>0){

            skus.forEach(sku->{

                String defaultImg="";
                for (Images image : sku.getImages()) {
                   if(image.getDefaultImg()==1){
                       defaultImg=image.getImgUrl();
                   }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);


                skuInfoService.save(skuInfoEntity);

                //5.2 sku图片信息
                List<SkuImagesEntity> collect1 = sku.getImages().stream().map(m -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                   skuImagesEntity.setId(skuInfoEntity.getSkuId());
                   skuImagesEntity.setImgUrl(m.getImgUrl());
                   skuImagesEntity.setDefaultImg(m.getDefaultImg());
                    return skuImagesEntity;

                }).filter((o)->{
                    return !StringUtils.isEmpty(o.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(collect1);


                //5.3 sku的销售属性
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> collect2 = attr.stream().map((o) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(o, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());

                    return skuSaleAttrValueEntity;

                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(collect2);

                //5.4 sku 优惠信息 远程调用

                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku,skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());

               if(skuReductionTo.getFullCount()>0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
                   couponFeignService.saveSkuReduction(skuReductionTo);
               }





            });
        }










    }

    private void saveSpuInfoDecr(SpuInfoDescEntity spuInfoDescEntity) {

        spuInfoDescDao.insert(spuInfoDescEntity);

    }

    @Override
    public void saveBaseSpuInfos(SpuInfoEntity spuInfoEntity) {

        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void up(Long id) throws IOException {


        List<SkuEsModel> list  =new ArrayList<>();



        //查出spu对应的sku
         List<SkuInfoEntity>  skuInfoEntities=      skuInfoService.getBySpuid(id);

        List<Long> skuIds = skuInfoEntities.stream().map(o -> {

            return o.getSkuId();
        }).collect(Collectors.toList());


        //查出spu所有能被检索的属性
        //先查出所有spu对应的属性 然后根据这些属性的id查出所有能被检索的属性id
        //然后过滤掉不能被检索的属性转成es的po里的attr
        List<ProductAttrValueEntity> productAttrValueEntities = attrValueService.baseAttrlistforspu(id);
        List<Long> ids = productAttrValueEntities.stream().map(o -> {

            return o.getAttrId();
        }).collect(Collectors.toList());



        if(ids==null || ids.size()==0){
            throw new RuntimeException("系统异常");
        }

          List<Long> searchIds=  attrService.getByAttrId(ids);

        List<SkuEsModel.Attrs> search = productAttrValueEntities.stream().filter(o -> {

            return searchIds.contains(o.getAttrId());
        }).map(o->{

            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();

            BeanUtils.copyProperties(o,attrs);

            return attrs;
        }).collect(Collectors.toList());

        //远程调用库存系统查询是否有库存
        //然后转成map key是sku的id value是是否有库存

        Map<Long, Boolean> hasStockMap = null;
        try{
            List<SkuHasStockVo> hasstock = wareFeignService.hasstock(skuIds);


         hasStockMap   = hasstock.stream().collect(Collectors.toMap(item -> item.getSkuId(), item -> item.getHasStock()));


        }
        catch (Exception e){



        }

        final Map<Long,Boolean> hasStockMap1 = hasStockMap;





        //把sku转成es的po
        List<SkuEsModel> collect = skuInfoEntities.stream().map((o) -> {

            SkuEsModel skuEsModel = new SkuEsModel();


            BeanUtils.copyProperties(o,skuEsModel);

            skuEsModel.setSkuPrice(o.getPrice());
            skuEsModel.setSkuImg(o.getSkuDefaultImg());


            if(hasStockMap1==null){
             skuEsModel.setHasStock(true);
            }
            else{
                skuEsModel.setHasStock(hasStockMap1.get(o.getSkuId()));
            }









            //todo 热度评分 用算法

            skuEsModel.setHotScore(0L);


            BrandEntity byId = brandService.getById(skuEsModel.getBrandId());

            skuEsModel.setBrandName(byId.getName());
            skuEsModel.setBrandImg(byId.getLogo());


            CategoryEntity byId1 = categoryService.getById(skuEsModel.getCatalogId());

            skuEsModel.setCatalogName(byId1.getName());

            skuEsModel.setAttrs(search);



            return skuEsModel;

        }).collect(Collectors.toList());

        //远程调用es微服务保存商品
        R up = esFeignService.up(collect);
        if(up.getCode()==0)
        this.baseMapper.updateSpuStatus(id, ProductConstant.ProductStatusEnum.SPU_UP.getCode());



      //业务幂等性


    }


}