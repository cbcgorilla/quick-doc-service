package cn.mxleader.quickdoc.web.restapi;

import cn.mxleader.quickdoc.web.domain.ErrorResponse;
import cn.mxleader.quickdoc.entities.QuickDocFolder;
import cn.mxleader.quickdoc.web.domain.RestResponse;
import cn.mxleader.quickdoc.web.domain.SuccessResponse;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.ReactiveFolderService;
import cn.mxleader.quickdoc.web.domain.WebFolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;


@RestController
@RequestMapping("/api/folder")
@Api(value = "Folder Configuration API", description = "目录配置修改接口")
public class FolderRestController {

    private final ReactiveFolderService reactiveFolderService;
    private final ConfigService configService;

    @Autowired
    FolderRestController(ReactiveFolderService reactiveFolderService,
                         ConfigService configService) {
        this.reactiveFolderService = reactiveFolderService;
        this.configService = configService;
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取根目录列表")
    public Flux<WebFolder> list() {
        return reactiveFolderService.findAllByParentIdInWebFormat(
                configService.getQuickDocHealth().getId());
    }

    @GetMapping("/list/{parentId}")
    @ApiOperation(value = "根据上级目录ID获取下级目录列表")
    public Flux<WebFolder> list(@PathVariable("parentId") ObjectId parentId) {
        return reactiveFolderService.findAllByParentIdInWebFormat(parentId);
    }

    @PostMapping(value = "/save",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存目录信息")
    public Mono<RestResponse> save(@RequestBody QuickDocFolder folder) {
        return reactiveFolderService.save(folder)
                .map(SuccessResponse::new);
    }

    @PostMapping(value = "/rename/{folderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更改目录名称")
    public Mono<RestResponse> rename(@PathVariable("folderId") ObjectId folderId,
                                     @RequestBody String newPath) {
        Optional<QuickDocFolder> folderOptional = reactiveFolderService.findById(folderId).blockOptional();
        if (folderOptional.isPresent()) {
            return reactiveFolderService.rename(folderOptional.get(), newPath)
                    .map(folder -> new SuccessResponse<>("目录改名成功！"));
        } else {
            return Mono.just(new ErrorResponse(0, "目录重命名失败, 请检查新目录名是否有误！"));
        }
    }

    @PostMapping(value = "/move/{folderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "移动目录位置")
    public Mono<RestResponse> move
            (@PathVariable("folderId") ObjectId folderId,
             @RequestBody String newFolderId) {
        Optional<QuickDocFolder> folderOptional = reactiveFolderService.findById(folderId).blockOptional();
        if (folderOptional.isPresent()) {
            return reactiveFolderService.move(folderId, new ObjectId(newFolderId))
                    .map(folder -> new SuccessResponse<>("目录转移成功！"));
        } else {
            return Mono.just(new ErrorResponse(0, "目录转移失败, 请检查新目录ID是否有误！"));
        }
    }

    @DeleteMapping(value = "/delete/{folderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "删除文件夹")
    public Mono<RestResponse> delete(@PathVariable ObjectId folderId) {
        return reactiveFolderService.delete(folderId)
                .map(v -> new SuccessResponse<>("删除文件夹成功！"));
    }

}
