package cn.mxleader.quickdoc.web.restcontroller;

import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rest/user-api")
@Api(value = "User API",description = "用户信息变更接口")
public class UserRestController {

    private final ReactiveUserService reactiveUserService;

    @Autowired
    UserRestController(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }
    // -------------------Retrieve All Users---------------------------------------------

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "返回所有用户信息清单")
    public Flux<UserEntity> listAllUsers() {
        return reactiveUserService.findAllUsers();/*
        if (users.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
            // You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<List<User>>(users, HttpStatus.OK);*/
    }

    // -------------------Retrieve Single User------------------------------------------

    @RequestMapping(value = "/get-user/{username}", method = RequestMethod.GET)
    @ApiOperation(value = "根据用户名返回用户信息详情")
    public Mono<UserEntity> getUser(@PathVariable("username") String username) {
        return reactiveUserService.findUser(username);
    }

    @PostMapping(value = "/save-user",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "新增或保存系统用户")
    public Mono<RestResponse<UserEntity>> addUser(
            @RequestBody UserEntity userEntity) {
        return reactiveUserService
                .saveUser(userEntity)
                .map(user -> new RestResponse<>(
                        "新增或保存系统用户",
                        RestResponse.CODE.SUCCESS, user));
    }

    @DeleteMapping(value = "/delete-user/{username}")
    @ApiOperation(value = "删除系统用户")
    public Mono<RestResponse> deleteUser(@PathVariable String username){
        reactiveUserService.deleteUserByUsername(username).subscribe();
        return Mono.just(new RestResponse("删除系统用户", RestResponse.CODE.SUCCESS,null));
    }

}
