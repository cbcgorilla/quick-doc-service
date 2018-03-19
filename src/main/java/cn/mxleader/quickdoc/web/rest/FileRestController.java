package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.web.domain.FolderTreeNode;
import cn.mxleader.quickdoc.web.domain.LayuiTable;
import cn.mxleader.quickdoc.web.domain.WebFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@Api(value = "File Rest API", description = "文件操作接口")
public class FileRestController {

    private final FileService fileService;

    FileRestController(FileService fileService){
        this.fileService = fileService;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "根据上级目录ID获取文件列表")
    public LayuiTable<WebFile> getFolderTree(@RequestParam String parentId) {
        List<WebFile> webFileList = fileService.list(new ParentLink(new ObjectId(parentId), ParentLink.PType.FOLDER));
        return new LayuiTable<>(0, "", webFileList.size(), webFileList);
    }

}
