package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.QuickDocFolder;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveFolderService;
import cn.mxleader.quickdoc.service.StreamService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static cn.mxleader.quickdoc.web.config.AuthenticationToolkit.*;
import static cn.mxleader.quickdoc.web.config.WebHandlerInterceptor.FOLDERS_ATTRIBUTE;

@Controller
@RequestMapping("/folders")
public class FolderController {

    private final Logger log = LoggerFactory.getLogger(FolderController.class);

    private final ReactiveFolderService reactiveFolderService;
    private final StreamService streamService;

    @Autowired
    public FolderController(ReactiveFolderService reactiveFolderService,
                            StreamService streamService) {
        this.reactiveFolderService = reactiveFolderService;
        this.streamService = streamService;
    }

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(HttpSession session, Model model) {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        Map<String, String> groupMap = new HashMap<>();
        String[] groups = activeUser.getGroups();
        if (groups != null) {
            for (String group : groups) {
                groupMap.put(group, group);
            }
        }
        model.addAttribute(FOLDERS_ATTRIBUTE, reactiveFolderService.findAllInWebFormat()
                .toStream().collect(Collectors.toList()));
        model.addAttribute("groupMap", groupMap);
        return "folders";
    }

    /**
     * 保存新增目录信息
     *
     * @param parentId
     * @param path
     * @param ownersRequest
     * @param shareGroups
     * @param redirectAttributes
     * @param session
     * @return
     */
    @PostMapping("/save")
    public String save(@RequestParam("parentId") String parentId,
                       @RequestParam("path") String path,
                       @RequestParam(value = "owners", required = false) String[] ownersRequest,
                       @RequestParam("shareGroups") String[] shareGroups,
                       RedirectAttributes redirectAttributes,
                       HttpSession session) {

        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        QuickDocFolder quickDocFolder = reactiveFolderService.findById(new ObjectId(parentId)).block();
        // 鉴权检查
        if (checkAuthentication(quickDocFolder.getOpenAccess(),
                quickDocFolder.getAuthorizations(),
                activeUser, WRITE_PRIVILEGE)) {
            reactiveFolderService.save(path, new ObjectId(parentId),
                    getOpenAccessFromOwnerRequest(ownersRequest),
                    translateOwnerRequest(activeUser, ownersRequest, shareGroups)).subscribe();

            // 发送MQ消息
            streamService.sendMessage("用户" + activeUser.getUsername() +
                    "成功添加目录： " + path + "到目录：" + parentId);
            redirectAttributes.addFlashAttribute("message",
                    "成功添加目录： " + path);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无此目录的权限： " + quickDocFolder.getPath() + "，请联系管理员获取！");
        }
        return "redirect:/folders";
    }

    /**
     * 删除文件夹
     *
     * @param folderId
     * @param session
     * @param redirectAttributes
     * @return
     */
    @DeleteMapping("/delete")
    public String delete(@RequestParam("folderId") ObjectId folderId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            reactiveFolderService.delete(folderId)
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe();
            redirectAttributes.addFlashAttribute("message",
                    "成功删除文件夹： " + folderId);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除文件夹的权限，请联系管理员获取！");
        }
        return "redirect:/folders";
    }

}
