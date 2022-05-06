package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;



    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model) {
        // 查询模型数据
        Map<String, Object> attributes = pageService.loadModel(spuId);
        model.addAllAttributes(attributes);

        //model.addAttribute("hello", "hello,thymeleaf");
        return "item";
    }
}
