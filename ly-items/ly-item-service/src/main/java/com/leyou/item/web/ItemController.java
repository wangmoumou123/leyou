package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Item;
import com.leyou.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<Item> saveItem(Item item) {
        if (item.getPrice() == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//            throw new RuntimeException("价格不能为空");
            throw new LyException(ExceptionEnum.PRICE_CAN_NOT_NULL);
        }
        Item item1 = itemService.saveItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(item1);
    }
}

