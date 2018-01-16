package cn.mxleader.quickdoc.web.restcontroller;

import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.FsCategory;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.web.dto.CategoryReplaceModel;
import cn.mxleader.quickdoc.web.dto.WebDirectory;
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
@Api(value = "Configuration API", description = "配置修改接口")
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

    @GetMapping("/category/list")
    @ApiOperation(value = "返回所有文件分类清单")
    public Flux<FsCategory> getCategories() {
        return reactiveCategoryService.findAll();
    }

    @PostMapping("/category/add/{type}")
    @ApiOperation(value = "新增文件分类信息")
    public Mono<RestResponse<FsCategory>> addCategory(@PathVariable String type) {
        return reactiveCategoryService.addCategory(type)
                .map(fsCategory -> new RestResponse<>(
                        ACTION_ADD_CATEGORY,
                        RestResponse.CODE.SUCCESS,
                        fsCategory));
    }

    @PostMapping(value = "/category/rename",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改目录名称")
    public Mono<RestResponse> renameCategory(
            @RequestBody CategoryReplaceModel replaceModel) {
        return reactiveCategoryService.renameCategory(replaceModel.getOldType(),
                replaceModel.getNewType())
                .flatMap(fsCategory -> {
                    return Mono.just(new RestResponse<>(
                            ACTION_RENAME_CATEGORY,
                            RestResponse.CODE.SUCCESS,
                            fsCategory));
                })
                .doOnError(v -> log.warn(v.getMessage()))
                .onErrorReturn(new RestResponse(
                        ACTION_RENAME_CATEGORY,
                        RestResponse.CODE.FAIL,
                        "文件重命名失败, 请检查已有文件名和新文件名是否有误！"));
    }

    @GetMapping("/directory/list")
    @ApiOperation(value = "获取根目录列表")
    public Flux<WebDirectory> getDirectories() {
        return reactiveDirectoryService.findAllByParentId(0L);
    }

    @GetMapping("/directory/list/{parentId}")
    @ApiOperation(value = "根据上级目录ID获取下级目录列表")
    public Flux<WebDirectory> getDirectories(@PathVariable("parentId") Long parentId) {
        return reactiveDirectoryService.findAllByParentId(parentId);
    }

    @PostMapping(value = "/directory/save",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存目录信息")
    public Mono<RestResponse<FsDirectory>> saveDirectory(@RequestBody FsDirectory directory) {
        return reactiveDirectoryService.saveDirectory(directory)
                .map(fsDirectory -> (new RestResponse<>(
                        ACTION_ADD_DIRECTORY,
                        RestResponse.CODE.SUCCESS,
                        fsDirectory)));
    }

}
