package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/folder")
@Api(value = "Folder Configuration API", description = "目录配置修改接口")
public class FolderRestController {

    private final FolderService folderService;

    @Autowired
    FolderRestController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/tree")
    @ApiOperation(value = "根据磁盘ID号获取目录树信息")
    public List<TreeNode> getFolderTree(@RequestParam String parentId) {
        return folderService.getFolderTree(new ParentLink(new ObjectId(parentId),
                AuthTarget.DISK, new ObjectId(parentId)));
    }

    @GetMapping("/list")
    @ApiOperation(value = "根据磁盘ID号获取目录列表")
    public List<TreeNode> getFoldersOfDisk(@RequestParam String diskId) {
        return folderService.listFoldersInDisk(new ObjectId(diskId))
                .stream()
                .map(sysFolder -> new TreeNode(sysFolder.getId().toString(), sysFolder.getName(),
                        sysFolder.getParent().getId().toString(), Collections.emptyList())
                )
                .collect(Collectors.toList());
    }

    @PostMapping("/save")
    @ApiOperation("根据上级目录ID增加子目录")
    public List<TreeNode> save(@RequestParam String name,
                               @RequestParam ObjectId parentId,
                               @RequestParam AuthTarget parentType,
                               @RequestParam ObjectId diskId) {
        //@TODO 增加对一个目录下增加同名文件夹的异常处理
        SysFolder sysFolder = folderService.save(name, new ParentLink(parentId, parentType, diskId));
        return new ArrayList<TreeNode>() {{
            add(new TreeNode(sysFolder.getId().toString(),
                    sysFolder.getName(),
                    sysFolder.getParent().getId().toString(),
                    Collections.emptyList()));
        }};
    }

    @PostMapping("/rename")
    @ApiOperation("修改目录名称")
    public TreeNode rename(@RequestParam ObjectId id, @RequestParam String newName) {
        try {
            SysFolder sysFolder = folderService.rename(id, newName);
            return new TreeNode(sysFolder.getId().toString(), sysFolder.getName(),
                    sysFolder.getParent().getId().toString(), Collections.emptyList());
        } catch (Exception exp) {
            //System.out.println(exp.getMessage());
            return null;
        }
    }

}
