package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthType;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final DiskService diskService;
    private final ConfigService configService;

    @Autowired
    public UserController(UserService userService,
                          DiskService diskService,
                          ConfigService configService) {
        this.userService = userService;
        this.diskService = diskService;
        this.configService = configService;
    }

    /**
     * 获取系统标题
     *
     * @return
     */
    @ModelAttribute("userAuthorityMap")
    public Map<SysUser.Authority, String> getUserAuthorityMap() {
        Map<SysUser.Authority, String> userAuthorityMap = new HashMap<>();
        userAuthorityMap.put(SysUser.Authority.USER, "普通用户");
        userAuthorityMap.put(SysUser.Authority.ADMIN, "系统管理员");
        return userAuthorityMap;
    }

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(Model model) {
        //model.addAttribute("users", userService.list());
        return "setting/users";
    }

    @GetMapping("/password")
    public String password(Model model) {
        return "setting/password";
    }

    @PostMapping("/save")
    public String save(@RequestParam("username") String username,
                       @RequestParam("title") String title,
                       @RequestParam("email") String email,
                       @RequestParam("password") String password,
                       @RequestParam("userGroup") String userGroup,
                       @RequestParam("userType") SysUser.Authority userType,
                       RedirectAttributes redirectAttributes,
                       Model model,
                       HttpSession session) {
        SysUser sysUser = new SysUser(ObjectId.get(), username, title, password,
                configService.getSysProfile().getIconMap().get("SYS_LOGO"),
                new HashSet<SysUser.Authority>() {{
                    add(userType);
                }},
                new HashSet<String>() {{
                    add(userGroup);
                }},
                email);
        if (userService.get(username) == null) {
            userService.saveUser(sysUser);
            diskService.save("我的磁盘1",
                    new Authorization(username, AuthType.PRIVATE,
                            new HashSet<AuthAction>() {{
                                add(AuthAction.READ);
                                add(AuthAction.WRITE);
                                add(AuthAction.DELETE);
                            }}));
            redirectAttributes.addFlashAttribute("message",
                    "保存用户信息成功： " + username);
        }
        return "redirect:/#users";
    }

}
