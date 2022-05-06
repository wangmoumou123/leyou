package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;


    public PageResult<Spu> querySpuByInitParams(Integer page, Integer rows, Boolean saleable, String key) {
        //开始分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //排序
        example.setOrderByClause("last_update_time DESC");
        List<Spu> spuList = spuMapper.selectByExample(example); //查询


        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //解析分类和品牌名称
        loadCategoryAndBrandName(spuList);
        // 解析结果
        PageInfo<Spu> pageInfo = new PageInfo<>(spuList);
        return new PageResult<>(pageInfo.getTotal(), spuList);


    }

    private void loadCategoryAndBrandName(List<Spu> spuList) {
        for (Spu spu : spuList) {
            // 解析分类名称,不如下面的好用
           /* categoryService.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).reduce((s, s1) -> s + s1);*/
            List<String> collect = categoryService.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(collect, "/"));

            //解析品牌名称
            spu.setBname(brandService.queryBtandById(spu.getBrandId()).getName());
        }
    }

    @Transient
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //定义库存集合
        List<Stock> stockList = new ArrayList<>();
        //新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        spuDetailMapper.insert(detail);
        //新增sku
        for (Sku sku : spu.getSkus()) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        System.out.println("==============================");
        System.out.println(stockList);

        int i = stockMapper.insertList(stockList);
        System.out.println("i==============================================>" + i);
        if (i == -1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 发送mq消息
        amqpTemplate.convertAndSend("item.insert", spu.getId());


    }

    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.SpuDetail_NOT_FOND);
        }
        return spuDetail;
    }

    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        System.out.println(spuId);
        System.out.println(sku);
        List<Sku> skuList = skuMapper.select(sku);
        System.out.println(skuList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.SKUS_NOT_FOND);
        }

        skuList.forEach(s -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });
        return skuList;
    }

    @Transient
    public void updateGoods(Spu spu) {
        // 查询以前sku
        List<Sku> skus = this.querySkusBySpuId(spu.getId());
        // 如果以前存在，则删除
        if (!CollectionUtils.isEmpty(skus)) {
            List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
            // 删除以前库存,多个一起删除
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);

            // 删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            this.skuMapper.delete(record);

        }
        // 新增sku和库存
        saveGoods(spu);
        // 发送mq消息
        amqpTemplate.convertAndSend("item.update", spu.getId());

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);

        // 更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.SPUS_NOT_FIND);
        }
        spu.setSkus(querySkusBySpuId(id));
        spu.setSpuDetail(querySpuDetailById(id));
        return spu;
    }

    public Sku querySkuById(Long id) {
        Sku sku = skuMapper.selectByPrimaryKey(id);
        if (sku == null) {
            throw new LyException(ExceptionEnum.SKUS_NOT_FOND);
        }
        return sku;
    }
}
