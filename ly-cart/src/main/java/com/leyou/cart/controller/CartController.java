package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<List<Cart>> queryCartList() {
        List<Cart> cartList = this.cartService.queryCartList();
        if (cartList == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(cartList);
    }


    @PutMapping
    public ResponseEntity<Void> changeNums(@RequestBody Cart cart) {
        this.cartService.changeNums(cart);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> delCarts(@PathVariable("skuId")long skuId){
        this.cartService.delCarts(skuId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
