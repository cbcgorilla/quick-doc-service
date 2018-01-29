package cn.mxleader.quickdoc.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
public class SwaggerConfig {
    private static final String REST_API_PACKAGE = "cn.mxleader.quickdoc.web";

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(REST_API_PACKAGE))
                .paths(regex("/api.*"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "QUICK DOC 快捷文档共享 REST API",
                "后端调用接口信息文档",
                "V1.0",
                "http://www.mxleader.cn",
                new Contact("Michael Chen", "http://www.mxleader.cn", "chenbichao@mxleader.cn"),
                "License of API", "http://www.mxleader.cn", Collections.emptyList());
    }
}
