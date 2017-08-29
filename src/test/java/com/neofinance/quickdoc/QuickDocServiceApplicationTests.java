package com.neofinance.quickdoc;

import com.neofinance.quickdoc.service.CategoryService;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(JUnitPlatform.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=SpringTestConfiguration.class)
public class QuickDocServiceApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Test
    public void contextLoads() {
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Test Category Service")
    public void testCategory() {
        assertNotNull(categoryService.addCategory("科技").subscribe());
        assertNotNull(categoryService.renameCategory("科技","人文").subscribe());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Test Category Service findOne by type")
    public void testCategoryFind() {
        assertNotNull(categoryService.findByType("科技").subscribe(System.out::println));
    }

}
