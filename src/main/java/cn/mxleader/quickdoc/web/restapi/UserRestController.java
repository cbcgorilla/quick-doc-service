package cn.mxleader.quickdoc.web.restapi;

import cn.mxleader.quickdoc.entities.RestResponse;
import cn.mxleader.quickdoc.entities.SuccessResponse;
import cn.mxleader.quickdoc.entities.QuickDocUser;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@Api(value = "User API", description = "用户信息变更接口")
public class UserRestController {

    private final ReactiveUserService reactiveUserService;

    @Autowired
    UserRestController(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }
    // -------------------Retrieve All Users---------------------------------------------

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "返回所有用户信息清单")
    public Flux<QuickDocUser> listAllUsers() {
        return reactiveUserService.findAllUsers();
    }

    // -------------------Retrieve Single User------------------------------------------

    @RequestMapping(value = "/get/{username}", method = RequestMethod.GET)
    @ApiOperation(value = "根据用户名返回用户信息详情")
    public Mono<QuickDocUser> getUser(@PathVariable("username") String username) {
        return reactiveUserService.findUser(username);
    }

    @PostMapping(value = "/save",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "新增或保存系统用户")
    public Mono<RestResponse> addUser(
            @RequestBody QuickDocUser quickDocUser) {
        return reactiveUserService.saveUser(quickDocUser)
                .map(SuccessResponse::new);
    }

    @DeleteMapping(value = "/delete/{username}")
    @ApiOperation(value = "删除系统用户")
    public Mono<RestResponse> deleteUser(@PathVariable String username) {
        reactiveUserService.deleteUserByUsername(username).subscribe();
        return Mono.just(new SuccessResponse<>("成功删除系统用户：" + username));
    }

}
