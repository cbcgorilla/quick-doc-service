package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import cn.mxleader.quickdoc.web.domain.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Api(value = "User API", description = "用户信息变更接口")
public class UserRestController {

    private final UserService userService;

    @Autowired
    UserRestController(UserService userService) {
        this.userService = userService;
    }
    // -------------------Retrieve All Users-------------------

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation("返回所有用户信息清单")
    public LayuiData<List<WebUser>> listAllUsers() {
        List<WebUser> users = userService.findAllUsers()
                .stream()
                .map(WebUser::new)
                .collect(Collectors.toList());
        return new LayuiData<>(0, "", users.size(), users);
    }

    // -------------------Retrieve Single User------------------------------------------

    @RequestMapping(value = "/get/{username}", method = RequestMethod.GET)
    @ApiOperation("根据用户名返回用户信息详情")
    public SysUser getUser(@PathVariable("username") String username) {
        return userService.findUser(username);
    }

    @PostMapping("/delete")
    @ApiOperation("删除系统用户")
    public Boolean deleteUser(@RequestBody String userId) {
        userService.deleteUserById(new ObjectId(userId));
        return true;
    }

    @PostMapping("/addGroup")
    @ApiOperation("批量添加用户组")
    public Boolean addGroup(@RequestParam String group,
                            @RequestBody List<WebUser> webUserList) {
        for (WebUser user : webUserList) {
            userService.addGroup(new ObjectId(user.getId()), group);
        }
        return true;
    }

    @PostMapping("/removeGroup")
    @ApiOperation("批量删除用户组")
    public Boolean removeGroup(@RequestParam String group,
                            @RequestBody List<WebUser> webUserList) {
        for (WebUser user : webUserList) {
            userService.removeGroup(new ObjectId(user.getId()), group);
        }
        return true;
    }

}
