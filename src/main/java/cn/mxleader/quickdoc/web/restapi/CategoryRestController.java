package cn.mxleader.quickdoc.web.restapi;

import cn.mxleader.quickdoc.entities.FsCategory;
import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.SuccessResponse;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.web.domain.RenameCategory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/category")
@Api(value = "File Category Configuration API", description = "文件分类配置修改接口")
public class CategoryRestController {

    private final ReactiveCategoryService reactiveCategoryService;

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
    public Mono<RestResponse> addCategory(@PathVariable String type) {
        return reactiveCategoryService.addCategory(type)
                .map(SuccessResponse::new);
    }

    @PostMapping(value = "/rename",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改文件分类名称")
    public Mono<RestResponse> renameCategory(@RequestBody RenameCategory replaceModel) {
        return reactiveCategoryService.renameCategory(
                replaceModel.getOldType(), replaceModel.getNewType())
                .map(fsCategory -> new SuccessResponse<>("文件分类改名成功！"));
    }

    @DeleteMapping(value = "/delete/{type}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "删除文件分类")
    public Mono<RestResponse> deleteCategory(@PathVariable String type) {
        return reactiveCategoryService.deleteCategory(type)
                .map(v -> new SuccessResponse<>("删除文件分类成功！"));
    }

}
