package com.leyou.page.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("items-service")
public interface SpecificationClient extends SpecificationApi {
}
