package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.common.utils.StringUtil;
import cn.mxleader.quickdoc.entities.FsDescription;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.service.StreamService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.AuthenticationHandler.*;
import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/directory")
public class AdminDirectoryController {

    private final Logger log = LoggerFactory.getLogger(AdminDirectoryController.class);

    private final ReactiveDirectoryService reactiveDirectoryService;
    private final StreamService streamService;

    @Autowired
    public AdminDirectoryController(ReactiveDirectoryService reactiveDirectoryService,
                                    StreamService streamService) {
        this.reactiveDirectoryService = reactiveDirectoryService;
        this.streamService = streamService;
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
     * 保存新增目录信息
     *
     * @param parentId
     * @param path
     * @param ownersRequest
     * @param redirectAttributes
     * @param model
     * @param session
     * @return
     */
    @PostMapping("/save")
    public String save(@RequestParam("parentId") String parentId,
                       @RequestParam("path") String path,
                       @RequestParam(value = "owners", required = false) String[] ownersRequest,
                       RedirectAttributes redirectAttributes,
                       Model model,
                       HttpSession session) {

        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        FsDirectory fsDirectory = reactiveDirectoryService.findById(new ObjectId(parentId)).block();
        // 鉴权检查
        if (checkAuthentication(fsDirectory.getPublicVisible(), fsDirectory.getOwners(), activeUser, WRITE_PRIVILEGE)) {
            reactiveDirectoryService.saveDirectory(path, new ObjectId(parentId),
                    getPublicVisibleFromOwnerRequest(ownersRequest),
                    translateOwnerRequest(activeUser, ownersRequest)).subscribe();

            // 发送MQ消息
            streamService.sendMessage("用户" + activeUser.getUsername() +
                    "成功添加目录： " + path + "到目录：" + parentId);
            redirectAttributes.addFlashAttribute("message",
                    "成功添加目录： " + path);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无此目录的权限： " + fsDirectory.getPath() + "，请联系管理员获取！");
        }
        return "redirect:/directory";
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
