package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.DiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/")
public class IndexController {

    private final DiskService diskService;

    @Autowired
    public IndexController(DiskService diskService) {
        this.diskService = diskService;
    }

    @GetMapping()
    public String index(Model model, HttpSession session) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        // 个人空间菜单
        AccessAuthorization privateAuthorization = new AccessAuthorization(activeUser.getUsername(),
                AccessAuthorization.Type.TYPE_PRIVATE,AccessAuthorization.Action.READ);
        model.addAttribute("private_disk_menu",diskService.list(privateAuthorization));

        //组共享空间菜单（包含该用户的所有权限组磁盘）
        List<SysDisk> groupSysDiskList = new ArrayList<>();
        for(String group: activeUser.getGroups()){
            AccessAuthorization groupAuthorization = new AccessAuthorization(group,
                    AccessAuthorization.Type.TYPE_GROUP,AccessAuthorization.Action.READ);
            groupSysDiskList.addAll(diskService.list(groupAuthorization));
        }
        model.addAttribute("group_disk_menu",groupSysDiskList);

        return "index";
    }

}
