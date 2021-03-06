package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.web.domain.LayuiData;
import cn.mxleader.quickdoc.web.domain.WebDisk;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@RestController
@RequestMapping("/api/disk")
//@Api(value = "Disk Rest API", description = "磁盘操作接口")
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
    //@ApiOperation(value = "获取磁盘清单")
    public LayuiData<List<WebDisk>> list(HttpSession session,
                                         @RequestParam Integer page,
                                         @RequestParam Integer limit) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        Page<SysDisk> diskPage = diskService.list(PageRequest.of(page - 1, limit));
        List<WebDisk> disks = diskPage.getContent()
                .stream()
                .map(WebDisk::new)
                .collect(Collectors.toList());
        if (activeUser.isAdmin()) {
            return new LayuiData<>(0, "", diskPage.getTotalElements(), disks);
        } else {
            return new LayuiData<>(1, "无权限获取磁盘列表", 0, null);
        }
    }

    /**
     * 获取磁盘清单
     *
     * @param page  当前页面编号（起始编号为1）
     * @param limit 每页显示数量限制
     * @return 返回LayUI标准Table数据格式
     */
    @GetMapping("/list1")
    //@ApiOperation(value = "获取磁盘清单")
    public LayuiData<List<WebDisk>> list1(HttpSession session,
                                          @RequestParam Integer page,
                                          @RequestParam Integer limit) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            Page<SysDisk> diskPage = diskService.list(PageRequest.of(page - 1, limit));
            List<WebDisk> disks = diskPage.getContent()
                    .stream()
                    .map(WebDisk::new)
                    .collect(Collectors.toList());
            return new LayuiData<>(0, "", diskPage.getTotalElements(), disks);
        } else if (activeUser.isManager()) {
            List<WebDisk> disks = diskService.list(new Authorization(activeUser.getUsername(),
                    AuthType.PRIVATE, AuthAction.ADMIN))
                    .stream()
                    .map(WebDisk::new)
                    .collect(Collectors.toList());
            return new LayuiData<>(0, "", disks.size(), disks);
        } else {
            return new LayuiData<>(1, "无权限获取磁盘列表", 0, null);
        }
    }

    /**
     * 获取磁盘授权清单
     *
     * @param page  当前页面编号（起始编号为1）
     * @param limit 每页显示数量限制
     * @return 返回LayUI标准Table数据格式
     */
    @GetMapping("/auth")
    //@ApiOperation(value = "根据上级目录ID获取文件列表")
    public LayuiData<Set<Authorization>> authList(@RequestParam ObjectId id,
                                                  @RequestParam Integer page,
                                                  @RequestParam Integer limit) {
        Optional<SysDisk> disk = diskService.get(id);
        if (disk.isPresent()) {
            Set<Authorization> auth = disk.get().getAuthorizations();
            return new LayuiData<>(0, "", auth.size(), auth);
        }
        return null;
    }

    @PostMapping("/addAuth")
    //@ApiOperation(value = "增加授权")
    public Boolean addAuth(@RequestParam ObjectId parentId,
                           @RequestBody Authorization authorization) {
        diskService.addAuthorization(parentId, authorization);
        return true;
    }

    @PostMapping("/removeAuth")
    //@ApiOperation(value = "移除授权")
    public Boolean removeAuth(@RequestParam ObjectId parentId,
                              @RequestBody Authorization authorization) {
        diskService.removeAuthorization(parentId, authorization);
        return true;
    }

    @PostMapping(value = "/rename")
    //@ApiOperation(value = "修改磁盘显示名称")
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
    //@ApiOperation(value = "根据磁盘ID删除库内磁盘信息")
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
