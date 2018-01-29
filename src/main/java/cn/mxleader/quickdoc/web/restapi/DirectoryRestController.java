package cn.mxleader.quickdoc.web.restapi;

import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.FsDirectory;
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
    public Mono<RestResponse<FsDirectory>> saveDirectory(@RequestBody FsDirectory directory) {
        return reactiveDirectoryService.saveDirectory(directory)
                .map(fsDirectory -> (new RestResponse<>(
                        "保存目录信息",
                        RestResponse.CODE.SUCCESS,
                        fsDirectory)));
    }

    @PostMapping(value = "/rename/{directoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改目录名称")
    public Mono<RestResponse<String>> renameDirectory
            (@PathVariable("directoryId") ObjectId directoryId,
             @RequestBody String newPath) {
        Optional<FsDirectory> fsDirectoryMono = reactiveDirectoryService.findById(directoryId).blockOptional();
        if (fsDirectoryMono.isPresent()) {
            return reactiveDirectoryService.renameDirectory(fsDirectoryMono.get(), newPath)
                    .flatMap(fsCategory -> Mono.just(
                            new RestResponse<>(
                                    "更改目录名称",
                                    RestResponse.CODE.SUCCESS,
                                    "目录改名成功！")))
                    .doOnError(v -> log.warn(v.getMessage()))
                    .onErrorReturn(new RestResponse<>(
                            "更改目录名称",
                            RestResponse.CODE.FAIL,
                            "目录重命名失败, 请检查新目录名是否有误！"));
        } else {
            return Mono.just(new RestResponse<>(
                    "更改目录名称",
                    RestResponse.CODE.FAIL,
                    "目录重命名失败, 请检查新目录名是否有误！"));
        }
    }

    @PostMapping(value = "/move/{directoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "移动目录位置")
    public Mono<RestResponse<String>> moveDirectory
            (@PathVariable("directoryId") ObjectId directoryId,
             @RequestBody String newDirectoryId) {
        Optional<FsDirectory> fsDirectoryMono = reactiveDirectoryService.findById(directoryId).blockOptional();
        if (fsDirectoryMono.isPresent()) {
            return reactiveDirectoryService.moveDirectory(directoryId, new ObjectId(newDirectoryId))
                    .flatMap(fsCategory -> Mono.just(
                            new RestResponse<>(
                                    "移动目录位置",
                                    RestResponse.CODE.SUCCESS,
                                    "目录转移成功！")))
                    .doOnError(v -> log.warn(v.getMessage()))
                    .onErrorReturn(new RestResponse<>(
                            "移动目录位置",
                            RestResponse.CODE.FAIL,
                            "目录转移失败, 请检查新目录ID是否有误！"));
        } else {
            return Mono.just(new RestResponse<>(
                    "移动目录位置",
                    RestResponse.CODE.FAIL,
                    "目录转移失败, 请检查新目录ID是否有误！"));
        }
    }

    @DeleteMapping(value = "/delete/{directoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "删除文件夹")
    public Mono<RestResponse<String>> deleteCategory(@PathVariable ObjectId directoryId) {
        return reactiveDirectoryService.deleteDirectory(directoryId)
                .flatMap(v -> Mono.just(new RestResponse<>(
                        "删除文件夹",
                        RestResponse.CODE.SUCCESS,
                        "删除文件夹成功！")))
                .doOnError(v -> log.warn(v.getMessage()))
                .onErrorReturn(new RestResponse<>(
                        "删除文件夹",
                        RestResponse.CODE.FAIL,
                        "删除文件夹失败, 请检查目录名是否有误！"));
    }

}
