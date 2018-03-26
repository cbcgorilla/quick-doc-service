package cn.mxleader.quickdoc.web.rest;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import cn.mxleader.quickdoc.web.domain.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Api(value = "User API", description = "用户信息变更接口")
public class UserRestController {

    private final UserService reactiveUserService;

    @Autowired
    UserRestController(UserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }
    // -------------------Retrieve All Users-------------------

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "返回所有用户信息清单")
    public LayuiData<List<WebUser>> listAllUsers() {
        List<WebUser> users = reactiveUserService.findAllUsers()
                .stream()
                .map(WebUser::new)
                .collect(Collectors.toList());
        return new LayuiData<>(0, "", users.size(), users);
    }

    // -------------------Retrieve Single User------------------------------------------

    @RequestMapping(value = "/get/{username}", method = RequestMethod.GET)
    @ApiOperation(value = "根据用户名返回用户信息详情")
    public SysUser getUser(@PathVariable("username") String username) {
        return reactiveUserService.findUser(username);
    }
/*
    @PostMapping(value = "/save",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "新增或保存系统用户")
    public RestResponse addUser(
            @RequestBody SysUser sysUser) {
        return new SuccessResponse<>(reactiveUserService.saveUser(sysUser));
    }

    @PostMapping(value = "/delete")
    @ApiOperation(value = "删除系统用户")
    public Boolean deleteUser(@RequestBody String userId) {
        reactiveUserService.deleteUserById(new ObjectId(userId));
        return true;
    }*/

}
