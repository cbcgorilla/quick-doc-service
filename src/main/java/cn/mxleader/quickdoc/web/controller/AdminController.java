package cn.mxleader.quickdoc.web.controller;

import cn.mxleader.quickdoc.service.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.HOME_TITLE;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReactiveUserService reactiveUserService;

    @Autowired
    public AdminController(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }

    /**
     * 获取系统标题
     *
     * @return
     */
    @ModelAttribute("title")
    public String pageTitle() {
        return HOME_TITLE;
    }

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(Model model) {
        model.addAttribute("message", "管理页面");
        model.addAttribute("users", reactiveUserService.findAllUsers()
                .toStream()
                .collect(Collectors.toList()));
        return "admin";
    }

    /**
     * 删除用户
     *
     * @param model
     * @return
     */
    @DeleteMapping("/deleteUser")
    public String deleteUser(Model model) {
        model.addAttribute("message", "管理页面");
        model.addAttribute("users", reactiveUserService.findAllUsers()
                .toStream()
                .collect(Collectors.toList()));
        return "admin";
    }

}
