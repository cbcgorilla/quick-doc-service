package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.security.session.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/directory")
public class AdminDirectoryController {

    private final Logger log = LoggerFactory.getLogger(AdminDirectoryController.class);

    private final ReactiveDirectoryService reactiveDirectoryService;

    @Autowired
    public AdminDirectoryController(ReactiveDirectoryService reactiveDirectoryService) {
        this.reactiveDirectoryService = reactiveDirectoryService;
    }

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(Model model) {
        model.addAttribute("directories", reactiveDirectoryService.findAllInWebFormat()
                .toStream()
                .collect(Collectors.toList()));
        return "directory";
    }

    /**
     * 删除文件夹
     *
     * @param directoryId
     * @param session
     * @param redirectAttributes
     * @return
     */
    @DeleteMapping("/deleteDirectory")
    public String deleteDirectory(@RequestParam("directoryId") ObjectId directoryId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            reactiveDirectoryService.deleteDirectory(directoryId)
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe();
            redirectAttributes.addFlashAttribute("message",
                    "成功删除文件夹： " + directoryId);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除文件夹的权限，请联系管理员获取！");
        }
        return "redirect:/directory";
    }

}
