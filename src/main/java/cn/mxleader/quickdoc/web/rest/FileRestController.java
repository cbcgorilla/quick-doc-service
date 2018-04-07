package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.common.utils.FileUtils;
import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.web.domain.LayuiData;
import cn.mxleader.quickdoc.web.domain.WebFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@Api(value = "File Rest API", description = "文件操作接口")
public class FileRestController {

    private final FileService fileService;
    private final FolderService folderService;

    FileRestController(FileService fileService,
                       FolderService folderService) {
        this.fileService = fileService;
        this.folderService = folderService;
    }

    /**
     * 根据上级ID和分类（DISK：磁盘，FOLDER：目录）获取文件清单
     *
     * @param parentId   上级ID
     * @param parentType 上级分类
     * @param page       当前页面编号（起始编号为1）
     * @param limit      每页显示数量限制
     * @return 返回LayUI标准Table数据格式
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "根据上级目录ID获取文件列表")
    public LayuiData<List<WebFile>> list(@RequestParam ObjectId parentId,
                                         @RequestParam AuthTarget parentType,
                                         @RequestParam Integer page,
                                         @RequestParam Integer limit) {
        Page<WebFile> filePage = fileService.list(getParentLink(parentId, parentType),
                PageRequest.of(page - 1, limit));
        return new LayuiData<>(0, "", filePage.getTotalElements(), filePage.getContent());
    }

    @PostMapping(value = "/upload")
    public LayuiData<Boolean> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam ObjectId parentId,
                                     @RequestParam AuthTarget parentType) throws IOException {
        //SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        String filename = FileUtils.getFilename(file.getOriginalFilename());
        ParentLink parent = getParentLink(parentId, parentType);
        if (fileService.getStoredFile(filename, parent) != null) {
            return new LayuiData<>(1, "文件名冲突", 0, false);
        }

        ObjectId fileId = fileService.store(file.getInputStream(), filename, parent);
        return new LayuiData<>(0, "", 0, true);
    }

    @PostMapping(value = "/delete")
    @ApiOperation(value = "根据文件ID删除库内文件信息")
    public Boolean delete(@RequestBody String fileId) {
        fileService.delete(new ObjectId(fileId));
        return true;
    }

    private ParentLink getParentLink(ObjectId parentId, AuthTarget parentType) {
        ParentLink parent = null;
        switch (parentType) {
            case DISK:
                parent = new ParentLink(parentId, parentType, parentId);
                break;
            case FOLDER:
                ObjectId diskId = folderService.get(parentId).get().getParent().getDiskId();
                parent = new ParentLink(parentId, parentType, diskId);
                break;
        }
        return parent;
    }

}
