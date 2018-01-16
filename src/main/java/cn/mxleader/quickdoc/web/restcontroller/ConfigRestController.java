package cn.mxleader.quickdoc.web.restcontroller;

import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.FsCategory;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.web.dto.CategoryReplaceModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/rest/config-api")
@Api(value = "Configuration API",description = "配置修改接口")
public class ConfigRestController {

    private static final String ACTION_ADD_CATEGORY = "新增文件分类名";
    private static final String ACTION_RENAME_CATEGORY = "修改文件分类名";
    private static final String ACTION_ADD_DIRECTORY = "新增文件目录";

    private final ReactiveCategoryService reactiveCategoryService;
    private final ReactiveDirectoryService reactiveDirectoryService;

    private final Logger log = LoggerFactory.getLogger(ConfigRestController.class);

    @Autowired
    ConfigRestController(ReactiveCategoryService reactiveCategoryService,
                         ReactiveDirectoryService reactiveDirectoryService) {
        this.reactiveCategoryService = reactiveCategoryService;
        this.reactiveDirectoryService = reactiveDirectoryService;
    }

    @GetMapping("/getCategories")
    @ApiOperation(value = "返回所有文件分类清单")
    public Flux<FsCategory> getCategories() {
        return reactiveCategoryService.findAll();
    }

    @PostMapping("/addCategory/{type}")
    @ApiOperation(value = "新增文件分类信息")
    public Mono<RestResponse<FsCategory>> addCategory(@PathVariable String type) {
        return reactiveCategoryService.addCategory(type)
                .map(fsCategory -> new RestResponse<>(
                        ACTION_ADD_CATEGORY,
                        RestResponse.CODE.SUCCESS,
                        fsCategory));
    }

    @PostMapping(value = "/addDirectory/{flag}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存目录信息")
    public Mono<RestResponse<FsDirectory>> saveDirectory(@PathVariable Boolean flag,
                                                         @RequestBody FsDirectory directory) {
        return reactiveDirectoryService.addDirectory(directory, flag)
                .map(fsDirectory -> (new RestResponse<>(
                        ACTION_ADD_DIRECTORY,
                        RestResponse.CODE.SUCCESS,
                        fsDirectory)));
    }

    @PostMapping(value = "/renameCategory",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改目录名称")
    public Mono<RestResponse> renameCategory(
            @RequestBody CategoryReplaceModel replaceModel) {
        return reactiveCategoryService.renameCategory(replaceModel.getOldType(),
                replaceModel.getNewType())
                .flatMap(fsCategory -> {return Mono.just(new RestResponse<>(
                        ACTION_RENAME_CATEGORY,
                        RestResponse.CODE.SUCCESS,
                        fsCategory));})
                .doOnError(v -> log.warn(v.getMessage()))
                .onErrorReturn(new RestResponse(
                            ACTION_RENAME_CATEGORY,
                            RestResponse.CODE.FAIL,
                            "文件重命名失败, 请检查已有文件名和新文件名是否有误！"));
    }

}
