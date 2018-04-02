package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.service.FolderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/disk")
public class DiskController {

    private final DiskService diskService;
    private final FolderService folderService;

    @Autowired
    public DiskController(DiskService diskService,
                          FolderService folderService) {
        this.diskService = diskService;
        this.folderService = folderService;
    }

    @RequestMapping("/{diskId}")
    public String diskFilesPage(@PathVariable ObjectId diskId, Model model)
            throws JsonProcessingException {
        Optional<SysDisk> disk = diskService.get(diskId);
        if (disk.isPresent()) {
            model.addAttribute("disk", disk.get());
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("folderTree",
                    mapper.writeValueAsString(folderService.getFolderTree(
                            new ParentLink(diskId, AuthTarget.DISK, diskId))));
        }
        return "disk";
    }

    @RequestMapping("/space")
    public String spaceManagement(Model model) {
        Map<AuthType, String> authTypeMap = new HashMap<AuthType, String>() {{
            put(AuthType.GROUP, "共享组");
            put(AuthType.PRIVATE, "个人用户");
        }};
        model.addAttribute("authTypeMap", authTypeMap);
        return "setting/space";
    }

    @PostMapping("/save")
    public String save(@RequestParam("diskName") String diskName,
                       @RequestParam("owner") String owner,
                       @RequestParam("authType") AuthType authType,
                       RedirectAttributes redirectAttributes,
                       Model model,
                       HttpSession session) {
        diskService.save(diskName, new Authorization(owner, authType));
        return "redirect:/#disk/space";
    }

}
