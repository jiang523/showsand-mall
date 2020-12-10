package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:jiangwc33446
 * @DATE:create in 19:25 2020/11/26
 * @Description: 商品搜索服务
 **/
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map  = new HashMap();
        //查询列表
        map.putAll(searchList(searchMap));
        //查询分组 商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);

        //查询品牌和规格
        String category = (String)searchMap.get("category");
        if (!"".equals(category)){
            map.putAll(searchBrandAndSpecList(category));
        }else {
            if (categoryList.size()>0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }


        map.put("categoryList",categoryList);
        return map;
    }

    private Map searchList(Map searchMap){
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions options = new HighlightOptions().addField("item_title");
        //前缀
        options.setSimplePrefix("<em style='color:red'>");
        //后缀
        options.setSimplePostfix("</em>");
        query.setHighlightOptions(options);

        /** 关键字查询 */
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        /** 按照商品分类查询  **/
        if (searchMap.get("category")!=null && !"".equals(searchMap.get("category"))){
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        /** 按照商品品牌查询  **/
        if (searchMap.get("brand")!=null && !"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        /** 按照规格过滤   **/
        if (searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map<String,String>)searchMap.get("spec");
            for (String key:specMap.keySet()){
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec"+key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }


        /****  获取高亮结果集  *********/
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry entry:entryList){
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
            if (highlights.size()>0 && highlights.get(0).getSnipplets().size()>0) {
                String title = highlights.get(0).getSnipplets().get(0);
                TbItem item = (TbItem) entry.getEntity();
                item.setTitle(title);
            }
        }
        map.put("rows",page.getContent());
        return map;
    }

    /**
     * 分组查询商品分类列表
     */
    private List searchCategoryList(Map searchMap){
        Query query = new SimpleQuery("*:*");
        //根据关键词查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //指定group by
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        List<String> list = new ArrayList();
        for (GroupEntry<TbItem> entry:content){
            //将分组结果添加到返回值
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     * @param category 商品分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long templateId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId!=null) {
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("brandList", brandList);
            map.put("specList", specList);
        }
        return map;
    }

}
