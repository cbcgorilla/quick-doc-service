package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.web.domain.LayuiTable;
import cn.mxleader.quickdoc.web.domain.WebFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@Api(value = "File Rest API", description = "文件操作接口")
public class FileRestController {

    private final FileService fileService;

    FileRestController(FileService fileService) {
        this.fileService = fileService;
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

    @PostMapping(value = "/delete")
    @ApiOperation(value = "根据文件ID删除库内文件信息")
    public Boolean delete(@RequestBody ObjectId fileId) {
        //username=username.replaceAll("\"","");
        try {
            fileService.delete(fileId);
        }catch (Exception exp){
            //System.out.println(exp.getMessage());
            return false;
        }
        return true;
    }

}
