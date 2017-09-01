package com.neofinance.quickdoc.web.rest;

import com.neofinance.quickdoc.common.entities.ApiResponseEntity;
import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.common.entities.WebUser;
import com.neofinance.quickdoc.service.ReactiveCategoryService;
import com.neofinance.quickdoc.service.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.neofinance.quickdoc.common.utils.KeyUtil.stringUUID;


@RestController
@RequestMapping("/config-api")
public class ConfigRestController {

    private static final String ACTION_ADD_CATEGORY = "新增文件分类目录名";
    private static final String ACTION_RENAME_CATEGORY = "修改文件分类目录名";

    private final ReactiveCategoryService reactiveCategoryService;
    private final UserAuthenticationService userAuthenticationService;

    @Autowired
    ConfigRestController(ReactiveCategoryService reactiveCategoryService,
                         UserAuthenticationService userAuthenticationService) {
        this.reactiveCategoryService = reactiveCategoryService;
        this.userAuthenticationService = userAuthenticationService;
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

    @RequestMapping("/adduser/{username}/{password}")
    public Mono<ApiResponseEntity<WebUser>> addUser(@PathVariable String username,
                                                    @PathVariable String password) {
        return userAuthenticationService
                .saveUser(new WebUser(stringUUID(), username,
                        password,new String[]{"ADMIN","USER"}))
                .map(user -> new ApiResponseEntity<WebUser>(
                        "新增系统用户",
                        ApiResponseEntity.Code.SUCCESS, user));
    }


}
