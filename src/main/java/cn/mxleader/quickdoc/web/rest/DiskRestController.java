package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysDisk;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.web.domain.LayuiData;
import cn.mxleader.quickdoc.web.domain.WebDisk;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    @GetMapping("/list")
    @ApiOperation(value = "根据上级目录ID获取文件列表")
    public LayuiData<List<WebDisk>> list(@RequestParam Integer page,
                                         @RequestParam Integer limit) {
        Page<SysDisk> diskPage = diskService.list(PageRequest.of(page - 1, limit));
        List<WebDisk> disks = diskPage.getContent()
                .stream()
                .map(WebDisk::new)
                .collect(Collectors.toList());
        return new LayuiData<>(0, "", diskPage.getTotalElements(), disks);
    }

    /**
     * 获取磁盘授权清单
     *
     * @param page  当前页面编号（起始编号为1）
     * @param limit 每页显示数量限制
     * @return 返回LayUI标准Table数据格式
     */
    @GetMapping("/auth")
    @ApiOperation(value = "根据上级目录ID获取文件列表")
    public LayuiData<Set<Authorization>> authList(@RequestParam ObjectId id,
                                            @RequestParam Integer page,
                                            @RequestParam Integer limit) {
        Optional<SysDisk> disk = diskService.get(id);
        if (disk.isPresent()) {
            Set<Authorization> auth = disk.get().getAuthorizations();
            return new LayuiData<>(0,"",auth.size(),auth);
        }
        return null;
    }

    @PostMapping(value = "/rename")
    @ApiOperation(value = "修改磁盘显示名称")
    public Boolean rename(@RequestParam ObjectId diskId, @RequestParam String newName) {
        try {
            diskService.rename(diskId, newName);
        } catch (Exception exp) {
            //System.out.println(exp.getMessage());
            return false;
        }
        return true;
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
