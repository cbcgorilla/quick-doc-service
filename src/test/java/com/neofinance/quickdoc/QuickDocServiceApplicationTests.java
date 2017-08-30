package com.neofinance.quickdoc;

import com.neofinance.quickdoc.service.ReactiveCategoryService;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(JUnitPlatform.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=SpringTestConfiguration.class)
public class QuickDocServiceApplicationTests {

    @Autowired
    ReactiveCategoryService reactiveCategoryService;

    @Test
    public void contextLoads() {
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Test Category Service")
    public void testCategory() {
        assertNotNull(reactiveCategoryService.addCategory("科技").subscribe());
        assertNotNull(reactiveCategoryService.renameCategory("科技","人文").subscribe());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Test Category Service findOne by type")
    public void testCategoryFind() {
        assertNotNull(reactiveCategoryService.findByType("科技").subscribe(System.out::println));
    }

}
