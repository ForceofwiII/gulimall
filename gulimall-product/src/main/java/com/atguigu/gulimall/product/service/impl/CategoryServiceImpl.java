package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;


    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
             categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());




        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> listWithTrees() {

        //查出所有分类
        List<CategoryEntity> all = categoryDao.selectList(null);



        //组装成父子的树形结构
        //找到所有的一级分类
        List<CategoryEntity> level1 = all.stream().filter((o) -> {
            return o.getParentCid() == 0;
        }).map((o)->{
            o.setChildren(getChildrens(o,all)); //获得当前元素的子元素
            return o;
        }).sorted(((o1, o2) -> {
            return (o1.getSort()==null?0: o1.getSort()) - (o2.getSort()==null?0:o2.getSort());
        })).collect(Collectors.toList());


        return  level1;
    }

    @Override
    public void removeMenuByList(List<Long> list) {


        //todo 判断当前商品是否被引用


        categoryDao.deleteBatchIds(list);


    }

    @Override
    public void insertOrUpdate(CategoryEntity category) {

        if(category.getCatId() == null){
            this.save(category);}

        else{
            this.updateById(category);
        }

    }

    @Override
    public List<CategoryEntity> getLevel1() {

        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid",0));

    }



    @Override
    public Map<String,List<Catelog2Vo>>  getCataLogJson(){

        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isEmpty(catalogJson)){
            //缓存中没有数据
            Map<String, List<Catelog2Vo>> categoryJsonFromDb = getCataLogJsonFromDb();


            stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(categoryJsonFromDb),1000, TimeUnit.SECONDS);
            return categoryJsonFromDb;
        }


        return      JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});



    }



    public Map<String, List<Catelog2Vo>> getCataLogJsonFromDb() {





        List<CategoryEntity> allLevel = this.list(null);

        List<CategoryEntity> level1 = this.getParent(allLevel, 0L);

        Map<String, List<Catelog2Vo>> parentCid = level1.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> {
            //返回所有二级分类
            List<CategoryEntity> level2 = this.getParent(allLevel, v.getCatId());

            List<Catelog2Vo> collect = null;
            if (level2 != null || level2.size() > 0) {
                collect = level2.stream().map(o -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, o.getCatId().toString(), o.getName());

                    //获取当前二级分类的三级分类
                    List<CategoryEntity> level3 = this.getParent(allLevel, o.getCatId());
                    if(level3!=null || level3.size()>0){
                        List<Catelog2Vo.Category3Vo> category3Vos = level3.stream().map(level3Cat -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(o.getCatId().toString(), level3Cat.getCatId().toString(), level3Cat.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }


                    return catelog2Vo;
                }).collect(Collectors.toList());


            }
            return collect;

        }));


        return parentCid;
    }



    private List<CategoryEntity> getParent(List<CategoryEntity> allLevel, Long parentId){

        List<CategoryEntity> collect = allLevel.stream().filter((o) -> {


            return o.getParentCid() == parentId;
        }).collect(Collectors.toList());


        return  collect;


    }



    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
//        paths.add(catelogId);
//        CategoryEntity byId = this.getById(catelogId);
//        if(byId.getParentCid()!=0){
//            findParentPath(byId.getParentCid(),paths);
//        }

        while(true){
            paths.add(catelogId);
            CategoryEntity byId1 = this.getById(catelogId);
            if(byId1.getParentCid() == 0){
                break;
            }
            catelogId = byId1.getParentCid();
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }



}