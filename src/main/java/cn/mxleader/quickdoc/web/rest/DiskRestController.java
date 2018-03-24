package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.web.domain.LayuiTable;
import cn.mxleader.quickdoc.web.domain.WebDisk;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/disk")
@Api(value = "Disk Rest API", description = "磁盘操作接口")
public class DiskRestController {
    private final DiskService diskService;

    DiskRestController(DiskService diskService) {
        this.diskService = diskService;
    }

    /**
     * 获取磁盘清单
     *
     * @param page  当前页面编号（起始编号为1）
     * @param limit 每页显示数量限制
     * @return 返回LayUI标准Table数据格式
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "根据上级目录ID获取文件列表")
    public LayuiTable<WebDisk> list(@RequestParam Integer page,
                                    @RequestParam Integer limit) {
        List<WebDisk> disks = diskService.list()
                .stream()
                .map(WebDisk::new)
                .collect(Collectors.toList());
        return new LayuiTable<>(0, "", disks.size(), disks);
    }

    @PostMapping(value = "/delete")
    @ApiOperation(value = "根据磁盘ID删除库内磁盘信息")
    public Boolean delete(@RequestBody String diskId) {
        try {
            diskService.delete(new ObjectId(diskId));
        } catch (Exception exp) {
            //System.out.println(exp.getMessage());
            return false;
        }
        return true;
    }


}
