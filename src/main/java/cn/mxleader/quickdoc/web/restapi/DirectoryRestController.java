package cn.mxleader.quickdoc.web.restapi;

import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.SuccessResponse;
import cn.mxleader.quickdoc.entities.ErrorResponse;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.service.QuickDocConfigService;
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
@RequestMapping("/api/directory")
@Api(value = "Directory Configuration API", description = "目录配置修改接口")
public class DirectoryRestController {

    private final ReactiveDirectoryService reactiveDirectoryService;
    private final QuickDocConfigService quickDocConfigService;

    private final Logger log = LoggerFactory.getLogger(DirectoryRestController.class);

    @Autowired
    DirectoryRestController(ReactiveDirectoryService reactiveDirectoryService,
                            QuickDocConfigService quickDocConfigService) {
        this.reactiveDirectoryService = reactiveDirectoryService;
        this.quickDocConfigService = quickDocConfigService;
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取根目录列表")
    public Flux<WebDirectory> getDirectories() {
        return reactiveDirectoryService.findAllByParentIdInWebFormat(
                quickDocConfigService.getQuickDocConfig().getId());
    }

    @GetMapping("/list/{parentId}")
    @ApiOperation(value = "根据上级目录ID获取下级目录列表")
    public Flux<WebDirectory> getDirectories(@PathVariable("parentId") ObjectId parentId) {
        return reactiveDirectoryService.findAllByParentIdInWebFormat(parentId);
    }

    @PostMapping(value = "/save",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存目录信息")
    public Mono<RestResponse> saveDirectory(@RequestBody FsDirectory directory) {
        return reactiveDirectoryService.saveDirectory(directory)
                .map(SuccessResponse::new);
    }

    @PostMapping(value = "/rename/{directoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改目录名称")
    public Mono<RestResponse> renameDirectory
            (@PathVariable("directoryId") ObjectId directoryId,
             @RequestBody String newPath) {
        Optional<FsDirectory> fsDirectoryOptional = reactiveDirectoryService.findById(directoryId).blockOptional();
        if (fsDirectoryOptional.isPresent()) {
            return reactiveDirectoryService.renameDirectory(fsDirectoryOptional.get(), newPath)
                    .map(fsCategory -> new SuccessResponse<>("目录改名成功！"));
        } else {
            return Mono.just(new ErrorResponse(0, "目录重命名失败, 请检查新目录名是否有误！"));
        }
    }

    @PostMapping(value = "/move/{directoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "移动目录位置")
    public Mono<RestResponse> moveDirectory
            (@PathVariable("directoryId") ObjectId directoryId,
             @RequestBody String newDirectoryId) {
        Optional<FsDirectory> fsDirectoryOptional = reactiveDirectoryService.findById(directoryId).blockOptional();
        if (fsDirectoryOptional.isPresent()) {
            return reactiveDirectoryService.moveDirectory(directoryId, new ObjectId(newDirectoryId))
                    .map(fsCategory -> new SuccessResponse<>("目录转移成功！"));
        } else {
            return Mono.just(new ErrorResponse(0, "目录转移失败, 请检查新目录ID是否有误！"));
        }
    }

    @DeleteMapping(value = "/delete/{directoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "删除文件夹")
    public Mono<RestResponse> deleteCategory(@PathVariable ObjectId directoryId) {
        return reactiveDirectoryService.deleteDirectory(directoryId)
                .map(v -> new SuccessResponse<>("删除文件夹成功！"));
    }

}
