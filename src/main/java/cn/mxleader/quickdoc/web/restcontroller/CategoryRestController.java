package cn.mxleader.quickdoc.web.restcontroller;

import cn.mxleader.quickdoc.entities.FsCategory;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.service.ReactiveQuickDocConfigService;
import cn.mxleader.quickdoc.web.domain.RenameCategory;
import cn.mxleader.quickdoc.web.domain.WebDirectory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;


@RestController
@RequestMapping("/rest/category-api")
@Api(value = "File Category Configuration API", description = "文件分类配置修改接口")
public class CategoryRestController {

    private final ReactiveCategoryService reactiveCategoryService;

    private final Logger log = LoggerFactory.getLogger(CategoryRestController.class);

    @Autowired
    CategoryRestController(ReactiveCategoryService reactiveCategoryService) {
        this.reactiveCategoryService = reactiveCategoryService;
    }

    @GetMapping("/list")
    @ApiOperation(value = "返回所有文件分类清单")
    public Flux<FsCategory> getCategories() {
        return reactiveCategoryService.findAll();
    }

    @PostMapping("/add/{type}")
    @ApiOperation(value = "新增文件分类信息")
    public Mono<RestResponse<FsCategory>> addCategory(@PathVariable String type) {
        return reactiveCategoryService.addCategory(type)
                .map(fsCategory -> new RestResponse<>(
                        "新增文件分类信息",
                        RestResponse.CODE.SUCCESS,
                        fsCategory));
    }

    @PostMapping(value = "/rename",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改文件分类名称")
    public Mono<RestResponse<String>> renameCategory(@RequestBody RenameCategory replaceModel) {
        return reactiveCategoryService.renameCategory(
                replaceModel.getOldType(), replaceModel.getNewType())
                .flatMap(fsCategory -> Mono.just(
                        new RestResponse<>(
                                "更改文件分类名称",
                                RestResponse.CODE.SUCCESS,
                                "文件分类改名成功！")))
                .doOnError(v -> log.warn(v.getMessage()))
                .onErrorReturn(new RestResponse<>(
                        "更改文件分类名称",
                        RestResponse.CODE.FAIL,
                        "文件分类重命名失败, 请检查新文件分类名是否有误！"));
    }

    @DeleteMapping(value = "/delete/{type}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "删除文件分类")
    public Mono<RestResponse<String>> deleteCategory(@PathVariable String type) {
        return reactiveCategoryService.deleteCategory(type)
                .flatMap(v -> Mono.just(new RestResponse<>(
                        "删除文件分类",
                        RestResponse.CODE.SUCCESS,
                        "删除文件分类成功！")))
                .doOnError(v -> log.warn(v.getMessage()))
                .onErrorReturn(new RestResponse<>(
                        "删除文件分类",
                        RestResponse.CODE.FAIL,
                        "删除文件分类失败, 请检查文件分类名是否有误！"));
    }

}
