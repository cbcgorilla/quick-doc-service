package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.security.session.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final ReactiveUserService reactiveUserService;

    @Autowired
    public AdminUserController(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }

    /**
     * 获取系统标题
     *
     * @return
     */
    @ModelAttribute("userTypeMap")
    public Map<String, String> getUserTypeMap() {
        Map<String, String> userTypeMap = new HashMap<>();
        userTypeMap.put("USER", "普通用户");
        userTypeMap.put("ADMIN", "系统管理员");
        return userTypeMap;
    }

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(Model model) {
        model.addAttribute("users", reactiveUserService.findAllUsers()
                .toStream()
                .collect(Collectors.toList()));
        return "admin";
    }

    @PostMapping("/addUser")
    public String addUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("userGroup") String userGroup,
                             @RequestParam("userType") String userType,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) {
        String[] authorities = new String[]{userType};
        UserEntity userEntity = new UserEntity(ObjectId.get(), username, password,
                authorities, authorities, new String[]{userGroup});
        reactiveUserService.saveUser(userEntity).subscribe();

        redirectAttributes.addFlashAttribute("message",
                "成功添加用户： " + username);
        return "redirect:/admin";
    }

    /**
     * 删除系统用户信息
     *
     * @param userId
     * @param session
     * @param redirectAttributes
     * @return
     */
    @DeleteMapping("/deleteUser")
    public String deleteUser(@RequestParam("userId") ObjectId userId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            reactiveUserService.deleteUserById(userId).subscribe();
            redirectAttributes.addFlashAttribute("message",
                    "成功删除用户： " + userId);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除用户的权限，请联系管理员获取！");
        }
        return "redirect:/admin";
    }

}
