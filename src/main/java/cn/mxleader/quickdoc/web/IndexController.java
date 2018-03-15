package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.web.domain.WebFolder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Controller
@RequestMapping("/sss")
public class IndexController {

    public static final String FOLDERS_MENU = "foldersMenu";

    private final FolderService folderService;
    private final ConfigService configService;

    @Autowired
    public IndexController(FolderService folderService,
                           ConfigService configService) {
        this.folderService = folderService;
        this.configService = configService;
    }
    @GetMapping()
    public String index(Model model, HttpSession session) {
        ObjectId rootParentId = configService.getSysProfile().getId();
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        if (activeUser.isAdmin()) {
            model.addAttribute(FOLDERS_MENU,
                    folderService.findAllByParentIdInWebFormat(rootParentId)
                            .toStream().collect(Collectors.toList()));
        } else {
            List<WebFolder> webFolders = folderService.findAllByParentIdInWebFormat(rootParentId)
                    .filter(webFolder -> webFolder.getName().equalsIgnoreCase("root"))
                    .toStream()
                    .collect(Collectors.toList());
            if (webFolders != null && webFolders.size() > 0) {
                for (WebFolder subFolder : webFolders) {
                    //model.addAttribute("currentFolder", subFolder);
                    model.addAttribute(FOLDERS_MENU,
                            folderService.findAllByParentIdInWebFormat(rootParentId)
                                    .toStream().collect(Collectors.toList()));
                }
            }
        }
        return "index";
    }

}
