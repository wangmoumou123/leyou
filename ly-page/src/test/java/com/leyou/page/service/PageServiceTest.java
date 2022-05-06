package com.leyou.page.service;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest extends TestCase {
    @Autowired
    private PageService pageService;

    @Test
    public void testCreateHtml() {
        pageService.createHtml(121L);

    }
}