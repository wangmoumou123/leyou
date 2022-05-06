package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private TemplateEngine templateEngine;


    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        Spu spu = goodsClient.querySpuById(spuId);
        List<Sku> skus = spu.getSkus();
        SpuDetail detail = spu.getSpuDetail();
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<Category> categories = categoryClient
                .queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<SpecGroup> specs = specificationClient.queryGroupByCid(spu.getCid3());

        System.out.println("specs========>" + specs);
        // 查询特殊的规格参数
        List<SpecParam> params = specificationClient.querySpecParamsByGid(null, spu.getCid3(), null);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });
//        System.out.println("title==============>"+spu.getTitle());
        model.put("title", spu.getTitle());
        model.put("spu", spu);
        model.put("subTitle", spu.getSubTitle());
        model.put("skus", skus);
        model.put("spuDetail", detail);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("specs", specs);
        model.put("paramMap", paramMap);

        return model;
    }

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        File file = new File("E:\\javastudy\\spring_cloud_stu\\static_html\\item", spuId + ".html");
        if (file.exists()) {
            file.delete();
        }
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("静态化失败");
        }


    }

    public void deleteHtml(Long spuId) {
        File file = new File("E:\\javastudy\\spring_cloud_stu\\static_html\\item", spuId + ".html");
        if (file.exists()) {
            file.delete();
        }
    }
}
