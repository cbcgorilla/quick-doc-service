package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ConfigService configService;

    @Autowired
    public UserController(UserService userService, ConfigService configService) {
        this.userService = userService;
        this.configService = configService;
    }

    /**
     * 获取系统标题
     *
     * @return
     */
    @ModelAttribute("userAuthorityMap")
    public Map<SysUser.Authorities, String> getUserAuthorityMap() {
        Map<SysUser.Authorities, String> userAuthorityMap = new HashMap<>();
        userAuthorityMap.put(SysUser.Authorities.USER, "普通用户");
        userAuthorityMap.put(SysUser.Authorities.ADMIN, "系统管理员");
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
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }

    @PostMapping("/save")
    public String save(@RequestParam("username") String username,
                       @RequestParam("title") String title,
                       @RequestParam("email") String email,
                       @RequestParam("password") String password,
                       @RequestParam("userGroup") String userGroup,
                       @RequestParam("userType") SysUser.Authorities userType,
                       RedirectAttributes redirectAttributes,
                       Model model,
                       HttpSession session) {
        List<SysUser.Authorities> authorities = Arrays.asList(userType);
        SysUser sysUser = new SysUser(ObjectId.get(), username, title, password,
                configService.getSysProfile().getIconMap().get("SYS_LOGO"),
                authorities, Arrays.asList(userGroup),
                email);
        if (userService.findUser(username) == null) {
            userService.saveUser(sysUser);

            redirectAttributes.addFlashAttribute("message",
                    "保存用户信息成功： " + username);
        }
        return "redirect:/#users";
    }

    /**
     * 删除系统用户信息
     *
     * @param userId
     * @param session
     * @param redirectAttributes
     * @return
     */
    @DeleteMapping("/delete")
    public String delete(@RequestParam("userId") ObjectId userId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            userService.deleteUserById(userId);
            redirectAttributes.addFlashAttribute("message",
                    "成功删除用户： " + userId);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除用户的权限，请联系管理员获取！");
        }
        return "redirect:/#users";
    }

}
