package com.leyou.item.api;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.data.annotation.Id;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {


    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailById(@PathVariable("spuId") Long spuId);


    @GetMapping("sku/list")
    List<Sku> querySkusBySpuId(@RequestParam("id") Long spuId);

    @GetMapping("spu/page")
    PageResult<Spu> querySpuByInitParams(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key") String key);

    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);


    @GetMapping("sku/{id}")
    Sku querySkuById(@PathVariable("id") Long id);
}
