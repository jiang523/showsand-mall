package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:jiangwc33446
 * @DATE:create in 19:25 2020/11/26
 * @Description:
 **/
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
//        Query query = new SimpleQuery("*:*");
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows",page.getContent());

        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions options = new HighlightOptions().addField("item_title");
        //前缀
        options.setSimplePrefix("<em style='color:red'>");
        //后缀
        options.setSimplePostfix("</em>");
        query.setHighlightOptions(options);
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
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
}
