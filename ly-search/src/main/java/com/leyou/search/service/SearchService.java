package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.Buffer;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();

        // 查询分类
        List<Category> categoryList = categoryClient
                .queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORIES_NOT_FOND);
        }
        List<String> names = categoryList.stream().map(Category::getName).collect(Collectors.toList());
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        // 搜索字段
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();
        // 查询sku
        System.out.println("spuId=====>" + spuId);
        List<Sku> skuList = goodsClient.querySkusBySpuId(spuId);
        System.out.println("skuList===>" + skuList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.SKUS_NOT_FOND);
        }
        List<Map<String, Object>> skus = new ArrayList<>();
        Set<Long> priceList = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("images", StringUtils.substringBefore(sku.getImages(), ","));
            skus.add(map);
            //处理价格
            priceList.add(sku.getPrice());
        }
        // 查找价格
        // Set<Long> priceList = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());
        // 查询规格参数
        List<SpecParam> specParams = specificationClient.querySpecParamsByGid(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        // 查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.SpuDetail_NOT_FOND);
        }
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格残水
        String specialSpec = spuDetail.getSpecialSpec();
        Map<Long, List<String>> stringListMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });

        // 规格参数, key为规格参数名字, value为规格参数的值
        HashMap<String, Object> specs = new HashMap<>();
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            Object value = "";
            //判断是否通用参数
            if (specParam.getGeneric()) {
                value = genericSpec.get(specParam.getId());
                //判断是否数值类型
                if (specParam.getNumeric()) {
                    //处理成段
                    value = chooseSegment(value.toString(), specParam);
                }
            } else {
                value = stringListMap.get(specParam.getId());
            }
            // 存入map
            specs.put(key, value);
        }

        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setAll(all);//  查询字段
        goods.setPrice(priceList);
        goods.setSkus(JsonUtils.serialize(skus));
        goods.setSpecs(specs);
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> searchGoodsBySearchRequest(SearchRequest searchRequest) {
        System.out.println(searchRequest.toString());
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)) {
            return null;
        }
        int page = searchRequest.getPage() - 1;
        int size = searchRequest.getSize();
        // 创建查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page, size));
        // 结果过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //根据字段查询
        // 查询条件
       QueryBuilder basicQuery = buildBaseQuery(searchRequest);
        nativeSearchQueryBuilder.withQuery(basicQuery);
        // 聚合 分类和品牌的信息
        //聚合分类
        String categoryAggName = "category_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 聚合品牌
        String brandAggName = "brand_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 查询
        AggregatedPage<Goods> goodsPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        // 解析结果
        // 分页结果
        long total = goodsPage.getTotalElements();
        int totalPages = goodsPage.getTotalPages();
        List<Goods> goodsList = goodsPage.getContent();
        //解析聚合结果
        Aggregations aggs = goodsPage.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));

        // 规格参数聚合
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 商品分类存在并且数量为1  可依聚合规格参数
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);

        }
        return new SearchResult(total, totalPages, goodsList, categories, brands, specs);


    }

    private QueryBuilder buildBaseQuery(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        //过滤条件
        Map<String, String> filter = searchRequest.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            // 处理key
            String key = entry.getKey();
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs." + key + ".keyword";
            }
            String value = entry.getValue();
            System.out.println("spec================>"+key+"===============>"+value);
            queryBuilder.filter(QueryBuilders.termQuery(key, value));
        }

        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 查询需要聚合的规格参数
        List<SpecParam> specParams = specificationClient.querySpecParamsByGid(null, cid, true);
        // 聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 带上查询条件
        queryBuilder.withQuery(basicQuery);
        // 聚合
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }
        // 获取结果
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);

        // 解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : specParams) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", options);
            specs.add(map);
        }


        return specs;
    }


    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return brandClient.queryBrandByIds(ids);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return categoryClient.queryCategoryByIds(ids);
        } catch (Exception e) {
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {

        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建索引库
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRepository.save(goods);

    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
