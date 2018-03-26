package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.common.utils.FileUtils;
import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.web.domain.LayuiResponse;
import cn.mxleader.quickdoc.web.domain.LayuiTable;
import cn.mxleader.quickdoc.web.domain.WebFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/file")
@Api(value = "File Rest API", description = "文件操作接口")
public class FileRestController {

    private final FileService fileService;
    private final FolderService folderService;
    private final DiskService diskService;

    FileRestController(FileService fileService,
                       FolderService folderService,
                       DiskService diskService) {
        this.fileService = fileService;
        this.folderService = folderService;
        this.diskService = diskService;
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
    public LayuiTable<WebFile> list(@RequestParam ObjectId parentId,
                                    @RequestParam AuthTarget parentType,
                                    @RequestParam Integer page,
                                    @RequestParam Integer limit) {
        List<WebFile> webFileList = fileService.list(new ParentLink(parentId, parentType));
        return new LayuiTable<>(0, "", webFileList.size(), webFileList);
    }

    @PostMapping(value = "/upload")
    public LayuiResponse<Boolean> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("containerId") ObjectId containerId,
                                         @RequestParam("containerType") AuthTarget containerType) throws IOException {
        //SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        String filename = FileUtils.getFilename(file.getOriginalFilename());
        String fileType = FileUtils.guessMimeType(filename);

        ParentLink parent = new ParentLink(containerId, containerType);
        if (fileService.getStoredFile(filename, parent) != null) {
            return new LayuiResponse<>(1, "文件名冲突", false);
        }
        Metadata metadata = new Metadata(fileType, Arrays.asList(parent),
                getParentAuthorization(parent), null);

        ObjectId fileId = fileService.store(file.getInputStream(), filename, metadata);
        return new LayuiResponse<>(0, "", true);
    }

    private List<AccessAuthorization> getParentAuthorization(ParentLink parent) {
        if (parent.getTarget().equals(AuthTarget.FOLDER)) {
            Optional<SysFolder> optionalSysFolder = folderService.get(parent.getId());
            if (optionalSysFolder.isPresent()) {
                return optionalSysFolder.get().getAuthorizations();
            }
        } else if (parent.getTarget().equals(AuthTarget.DISK)) {
            Optional<SysDisk> optionalSysDisk = diskService.get(parent.getId());
            if (optionalSysDisk.isPresent()) {
                return optionalSysDisk.get().getAuthorizations();
            }
        }
        return null;
    }

    @PostMapping(value = "/delete")
    @ApiOperation(value = "根据文件ID删除库内文件信息")
    public Boolean delete(@RequestBody String fileId) {
        try {
            fileService.delete(new ObjectId(fileId));
        } catch (Exception exp) {
            //System.out.println(exp.getMessage());
            return false;
        }
        return true;
    }

}
