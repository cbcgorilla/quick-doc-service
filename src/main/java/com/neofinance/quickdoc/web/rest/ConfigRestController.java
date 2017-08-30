package com.neofinance.quickdoc.web.rest;

import com.neofinance.quickdoc.common.entities.ApiResponseEntity;
import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.service.ReactiveCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/config-api")
public class ConfigRestController {

    private static final String ACTION_ADD_CATEGORY = "新增文件分类目录名";
    private static final String ACTION_RENAME_CATEGORY = "修改文件分类目录名";

    private final ReactiveCategoryService reactiveCategoryService;

    @Autowired
    ConfigRestController(ReactiveCategoryService reactiveCategoryService) {
        this.reactiveCategoryService = reactiveCategoryService;
    }

    @RequestMapping()
    public Flux<FsCategory> getCategories() {
        return reactiveCategoryService.findAll();
    }

    @RequestMapping("/{type}")
    public Mono<ApiResponseEntity<FsCategory>> addCategory(@PathVariable String type) {
        return reactiveCategoryService.addCategory(type)
                .map(fsCategory -> new ApiResponseEntity<FsCategory>(
                        ACTION_ADD_CATEGORY,
                        ApiResponseEntity.Code.SUCCESS,
                        fsCategory));
    }

    @PostMapping(value = "/rename/{oldtype}_{newtype}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApiResponseEntity<FsCategory>> renameCategory(
            @PathVariable String oldtype,
            @PathVariable String newtype) {
        return reactiveCategoryService.renameCategory(oldtype, newtype)
                .map(fsCategory -> new ApiResponseEntity<FsCategory>(
                        ACTION_RENAME_CATEGORY,
                        ApiResponseEntity.Code.SUCCESS,
                        fsCategory))
                .onErrorReturn(new ApiResponseEntity<FsCategory>(
                        ACTION_RENAME_CATEGORY,
                        ApiResponseEntity.Code.FAIL,
                        null));
    }


}
