package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient("items-service")
public interface GoodsClient extends GoodsApi {


}
