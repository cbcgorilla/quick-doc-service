package cn.techfan.quickdoc.web.rest;

import cn.techfan.quickdoc.common.utils.KeyUtil;
import cn.techfan.quickdoc.entities.ApiResponseEntity;
import cn.techfan.quickdoc.entities.FsCategory;
import cn.techfan.quickdoc.entities.FsDirectory;
import cn.techfan.quickdoc.entities.UserEntity;
import cn.techfan.quickdoc.service.ReactiveCategoryService;
import cn.techfan.quickdoc.service.ReactiveDirectoryService;
import cn.techfan.quickdoc.service.UserAuthenticationService;
import cn.techfan.quickdoc.web.dto.CategoryReplaceModel;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@Log
@RestController
@RequestMapping("/rest/config-api")
public class ConfigRestController {

    private static final String ACTION_ADD_CATEGORY = "新增文件分类名";
    private static final String ACTION_RENAME_CATEGORY = "修改文件分类名";
    private static final String ACTION_ADD_DIRECTORY = "新增文件目录";

    private final ReactiveCategoryService reactiveCategoryService;
    private final ReactiveDirectoryService reactiveDirectoryService;
    private final UserAuthenticationService userAuthenticationService;

    @Autowired
    ConfigRestController(ReactiveCategoryService reactiveCategoryService,
                         ReactiveDirectoryService reactiveDirectoryService,
                         UserAuthenticationService userAuthenticationService) {
        this.reactiveCategoryService = reactiveCategoryService;
        this.reactiveDirectoryService = reactiveDirectoryService;
        this.userAuthenticationService = userAuthenticationService;
    }

    @RequestMapping("/getCategories")
    public Flux<FsCategory> getCategories() {
        return reactiveCategoryService.findAll();
    }

    @RequestMapping("/addCategory/{type}")
    public Mono<ApiResponseEntity<FsCategory>> addCategory(@PathVariable String type) {
        return reactiveCategoryService.addCategory(type)
                .map(fsCategory -> new ApiResponseEntity<>(
                        ACTION_ADD_CATEGORY,
                        ApiResponseEntity.Code.SUCCESS,
                        fsCategory));
    }

    @PostMapping(value = "/addDirectory/{flag}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApiResponseEntity<FsDirectory>> saveDirectory(@PathVariable Boolean flag,
                                                             @RequestBody FsDirectory directory) {
        return reactiveDirectoryService.addDirectory(directory, flag)
                .map(fsDirectory -> (new ApiResponseEntity<>(
                        ACTION_ADD_DIRECTORY,
                        ApiResponseEntity.Code.SUCCESS,
                        fsDirectory)));
    }

    @PostMapping(value = "/renameCategory",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public Mono<ApiResponseEntity> renameCategory(@RequestBody CategoryReplaceModel replaceModel) {
        return reactiveCategoryService.renameCategory(replaceModel.getOldType(),
                replaceModel.getNewType())
                .map(fsCategory -> (new ApiResponseEntity<>(
                        ACTION_RENAME_CATEGORY,
                        ApiResponseEntity.Code.SUCCESS,
                        fsCategory)))
                .doOnError(v -> log.log(Level.WARNING, v.getMessage()))
                .onErrorReturn(new ApiResponseEntity(
                        ACTION_RENAME_CATEGORY,
                        ApiResponseEntity.Code.FAIL,
                        "文件重命名失败, 请检查已有文件名和新文件名是否有误！"));
    }

    @PostMapping(value = "/addUser",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApiResponseEntity<UserEntity>> addUser(@RequestBody UserEntity userEntity) {
        return userAuthenticationService
                .saveUser(new UserEntity(KeyUtil.stringUUID(), userEntity.getUsername(),
                                userEntity.getPassword(), userEntity.getAuthorities()),
                        true)
                .map(user -> new ApiResponseEntity<>(
                        "新增系统用户",
                        ApiResponseEntity.Code.SUCCESS, user));
    }

}
