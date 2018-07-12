package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.common.utils.HanyuPinyinUtil;
import cn.mxleader.quickdoc.entities.SysProfile;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.UserService;
import cn.mxleader.quickdoc.web.domain.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@RestController
@RequestMapping("/api/user")
//@Api(value = "User API", description = "用户信息变更接口")
public class UserRestController {

    private final UserService userService;
    private final ConfigService configService;

    @Autowired
    UserRestController(UserService userService, ConfigService configService) {
        this.userService = userService;
        this.configService = configService;
    }
    // -------------------Retrieve All Users-------------------

    @GetMapping(value = "/list")
    //@ApiOperation("返回所有用户信息清单")
    public LayuiData<List<WebUser>> list(@RequestParam Integer page,
                                         @RequestParam Integer limit) {
        Page<SysUser> userPage = userService.list(PageRequest.of(page - 1, limit));
        List<WebUser> users = userPage.getContent()
                .stream()
                .map(WebUser::new)
                .collect(Collectors.toList());
        return new LayuiData<>(0, "", userPage.getTotalElements(), users);
    }

    // -------------------Retrieve Single User------------------------------------------

    @GetMapping("/get/{username}")
    //@ApiOperation("根据用户名返回用户信息详情")
    public SysUser getUser(@PathVariable("username") String username) {
        return userService.get(username);
    }

    @PostMapping("/save")
    public Boolean saveUser(@RequestBody WebUser webUser) {

        SysProfile sysProfile = configService.getSysProfile();
        userService.saveUser(new SysUser(ObjectId.get(), webUser.getUsername(),
                webUser.getTitle(), HanyuPinyinUtil.toHanyuPinyin(webUser.getTitle()),
                sysProfile.getIconMap().get("AWARD"),
                new HashSet<SysUser.Authority>() {{
                    add(SysUser.Authority.USER);
                }},
                webUser.getGroups(),
                webUser.getEmail()));
        return true;
    }

    @PostMapping("/update/{field}")
    //@ApiOperation("更新系统用户信息")
    public Boolean update(@PathVariable String field, @RequestBody WebUser webUser) {
        if (field.equalsIgnoreCase("username")) {
            if (userService.get(webUser.getUsername()) == null) {
                userService.update(webUser);
                return true;
            }
        } else {
            userService.update(webUser);
            return true;
        }
        return false;
    }

    @PostMapping("/changePassword")
    //@ApiOperation("修改密码")
    public LayuiData<Boolean> changePassword(@RequestParam String password,
                                             @RequestParam String newPassword,
                                             @RequestParam String verifyPassword,
                                             @SessionAttribute(SESSION_USER) SysUser user) {
        if (userService.validateUser(user.getUsername(), password) && newPassword.equals(verifyPassword)) {
            SysUser sysUser = userService.get(user.getUsername());
            userService.changePassword(sysUser.getId(), newPassword);
            return new LayuiData<>(0, "", 0, true);
        } else {
            return new LayuiData<>(0, "", 0, false);
        }
    }

    @PostMapping("/delete")
    //@ApiOperation("删除系统用户")
    public Boolean delete(@RequestBody String userId) {
        userService.delete(new ObjectId(userId));
        return true;
    }

    @PostMapping("/addGroup")
    //@ApiOperation("批量添加用户组")
    public Boolean addGroup(@RequestParam String group,
                            @RequestBody List<WebUser> webUserList) {
        for (WebUser user : webUserList) {
            userService.addGroup(new ObjectId(user.getId()), group);
        }
        return true;
    }

    @PostMapping("/removeGroup")
    //@ApiOperation("批量删除用户组")
    public Boolean removeGroup(@RequestParam String group,
                               @RequestBody List<WebUser> webUserList) {
        for (WebUser user : webUserList) {
            userService.removeGroup(new ObjectId(user.getId()), group);
        }
        return true;
    }

}
