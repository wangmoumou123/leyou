package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.service.SearchService;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest extends TestCase {
    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;


    @Test
    public void testCreateIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);

    }

    @Test
    public void loadData() {
        int page = 1;
        int rows = 100;
        int size = rows;
        while (size == 100) {
            // 查询spu
            PageResult<Spu> result = goodsClient.querySpuByInitParams(page, rows, true, "");

            List<Spu> spuList = result.getItems();
            //构建goods
            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());

            //存入 elasticsearch
            repository.saveAll(goodsList);
            size = spuList.size();
            page++;
        }
    }
}