package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthType;
import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.service.UserService;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
    public String index(Model model, HttpSession session) {
        //model.addAttribute("users", userService.list());
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            return "setting/users";
        } else if (activeUser.isManager()) {
            Set<TreeNode> deptTreeSet = new HashSet<>(configService.getSysProfile().getDepartments());
            Set<TreeNode> tempSet = new HashSet<>();
            for (TreeNode node : deptTreeSet) {
                if (node.getName().contains("(删除)")) {
                    removeChildren(deptTreeSet, tempSet, node);
                }
            }
            for(TreeNode node:tempSet){
                deptTreeSet.remove(node);
            }
            model.addAttribute("deptTree", deptTreeSet.stream()
                    .filter(node -> matchTreePath(node.getCompletePath(), activeUser.getManagePath()))
                    .collect(Collectors.toList()));

            model.addAttribute("rootPath", configService.getSysProfile().getCompanyName());
            return "setting/deptUsers";
        } else {
            return "redirect:/#index";
        }
    }

    @GetMapping("/auth")
    public String auth(@RequestParam("id") ObjectId id, Model model, HttpSession session) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        List<TreeNode> deptList = configService.getSysProfile().getDepartments();
        model.addAttribute("id", id.toString());
        if (activeUser.isAdmin()) {
            model.addAttribute("deptTree", deptList);
            return "setting/userAuth";
        } else if (activeUser.isManager()) {
            Set<TreeNode> deptTreeSet = new HashSet<>(deptList);
            Set<TreeNode> tempSet = new HashSet<>();
            for (TreeNode node : deptTreeSet) {
                if (node.getName().contains("(删除)")) {
                    removeChildren(deptTreeSet, tempSet, node);
                }
            }
            for(TreeNode node:tempSet){
                deptTreeSet.remove(node);
            }
            model.addAttribute("deptTree", deptTreeSet.stream()
                    .filter(node -> matchTreePath(node.getCompletePath(), activeUser.getManagePath()))
                    .collect(Collectors.toList()));
            return "setting/userAuth";
        } else {
            return "redirect:/#index";
        }
    }

    private static void removeChildren(Set<TreeNode> tree, Set<TreeNode> tempSet, TreeNode parent) {
        for (TreeNode node : tree) {
            if (node.getParentId().equalsIgnoreCase(parent.getId())) {
                removeChildren(tree, tempSet, node);
            }
        }
        tempSet.add(parent);
    }

    @GetMapping("/password")
    public String password(Model model) {
        return "setting/password";
    }

    @PostMapping("/save")
    public String save(@RequestParam("username") String username,
                       @RequestParam("displayName") String displayName,
                       @RequestParam("title") String title,
                       @RequestParam("email") String email,
                       @RequestParam("password") String password,
                       @RequestParam("userGroup") String userGroup,
                       @RequestParam("userType") SysUser.Authority userType,
                       RedirectAttributes redirectAttributes) {
        SysUser sysUser = new SysUser(ObjectId.get(), username, displayName, title, password,
                configService.getSysProfile().getIconMap().get("SYS_LOGO"), false, "",
                new HashSet<SysUser.Authority>() {{
                    add(userType);
                }},
                new HashSet<>(),
                new HashSet<String>() {{
                    add(userGroup);
                }},
                email);
        if (userService.get(username) == null) {
            userService.saveUser(sysUser);
            diskService.save("我的磁盘",
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

    /**
     * 匹配部门树授权节点及子节点
     *
     * @param srcPath
     * @param checkPath
     * @return
     */
    private static Boolean matchTreePath(String srcPath, String checkPath) {
        String[] nodes = checkPath.split("-");
        if (srcPath.length() <= checkPath.length()) {
            if (srcPath.equals(nodes[0])) {
                return true;
            }
            for (int i = 1; i < nodes.length; i++) {
                nodes[i] = nodes[i - 1] + "-" + nodes[i];
                if (srcPath.equals(nodes[i])) {
                    return true;
                }
            }
            return false;
        } else {
            return srcPath.startsWith(checkPath);
        }
    }

    private static Boolean matchTreePath(String srcPath, Set<String> checkPaths) {
        for (String checkPath : checkPaths) {
            if (matchTreePath(srcPath, checkPath))
                return true;
        }
        return false;
    }

}
