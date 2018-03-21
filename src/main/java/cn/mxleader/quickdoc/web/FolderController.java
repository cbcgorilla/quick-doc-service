package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.FolderService;
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

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/folders")
public class FolderController {

    private final Logger log = LoggerFactory.getLogger(FolderController.class);

    private final FolderService folderService;
    private final StreamService streamService;

    @Autowired
    public FolderController(FolderService folderService,
                            StreamService streamService) {
        this.folderService = folderService;
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
        /*model.addAttribute(FOLDERS_ATTRIBUTE, folderService.findAllInWebFormat()
                .toStream().collect(Collectors.toList()));*/
        return "folders";
    }

    /**
     * 保存新增目录信息
     *
     * @param folderIdRef        待修改目录的ID
     * @param parentId           新建子目录的上级目录
     * @param path               目录名称
     * @param shareSetting       共享设置选项
     * @param shareGroups        共享组选项
     * @param redirectAttributes
     * @param session
     * @return
     */
    @PostMapping("/save")
    public String save(@RequestParam(value = "folderIdRef", required = false) String folderIdRef,
                       @RequestParam(value = "parentId", required = false) String parentId,
                       @RequestParam("path") String path,
                       @RequestParam(value = "shareSetting", required = false) String[] shareSetting,
                       @RequestParam("shareGroups") String[] shareGroups,
                       RedirectAttributes redirectAttributes,
                       HttpSession session) {

        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        ObjectId folderId = folderIdRef != null && folderIdRef.trim().length() > 0 ?
                new ObjectId(folderIdRef) : new ObjectId(parentId);
        SysFolder folder = folderService.findById(folderId).get();
        // 鉴权检查
       /* if (checkAuthentication(folder.getAuthorizations(),
                activeUser, AccessAuthorization.Action.WRITE)) {

            if (folderIdRef != null && folderIdRef.trim().length() > 0) {
                // folderIdRef字段有数据则修改现有目录的数据
                folderService.save(new ObjectId(folderIdRef), path,
                        translateShareSetting(activeUser, shareSetting, shareGroups)).subscribe();
            } else {
                // folderIdRef字段无数据则增加新的子目录
                folderService.save(path, new ObjectId(parentId),
                        translateShareSetting(activeUser, shareSetting, shareGroups)).subscribe();
            }
            // 发送MQ消息
            streamService.sendMessage("用户" + activeUser.getUsername() +
                    "成功保存目录： " + path);
            redirectAttributes.addFlashAttribute("message",
                    "成功保存目录： " + path);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无此目录的权限： " + folder.getName() + "，请联系管理员获取！");
        }*/
        return "redirect:/#folders";
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
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            folderService.delete(folderId);
            redirectAttributes.addFlashAttribute("message",
                    "成功删除文件夹： " + folderId);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除文件夹的权限，请联系管理员获取！");
        }
        return "redirect:/#folders";
    }

}
