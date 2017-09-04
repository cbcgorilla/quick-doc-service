package cn.techfan.quickdoc.web.rest;

import cn.techfan.quickdoc.common.entities.ApiResponseEntity;
import cn.techfan.quickdoc.common.entities.FsCategory;
import cn.techfan.quickdoc.common.entities.WebUser;
import cn.techfan.quickdoc.common.utils.KeyUtil;
import cn.techfan.quickdoc.service.UserAuthenticationService;
import cn.techfan.quickdoc.service.ReactiveCategoryService;
import lombok.extern.java.Log;

import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log
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

    @PostMapping(value = "/rename/{oldtype}_{newtype}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public Mono<ApiResponseEntity> renameCategory(
            @PathVariable String oldtype,
            @PathVariable String newtype) {
        return reactiveCategoryService.renameCategory(oldtype, newtype)
                .map(fsCategory -> (new ApiResponseEntity<FsCategory>(
                        ACTION_RENAME_CATEGORY,
                        ApiResponseEntity.Code.SUCCESS,
                        fsCategory)))
                .doOnError(v -> log.log(Level.WARNING, v.getMessage()))
                .onErrorReturn(new ApiResponseEntity(
                        ACTION_RENAME_CATEGORY,
                        ApiResponseEntity.Code.FAIL,
                        "与已有文件分类冲突"));
    }

    @RequestMapping("/adduser/{username}/{password}/{privilege}")
    public Mono<ApiResponseEntity<WebUser>> addUser(@PathVariable String username,
                                                    @PathVariable String password,
                                                    @PathVariable String privilege) {
        return userAuthenticationService
                .saveUser(new WebUser(KeyUtil.stringUUID(), username,
                        password, new String[]{privilege, "USER"}))
                .map(user -> new ApiResponseEntity<WebUser>(
                        "新增系统用户",
                        ApiResponseEntity.Code.SUCCESS, user));
    }


}
