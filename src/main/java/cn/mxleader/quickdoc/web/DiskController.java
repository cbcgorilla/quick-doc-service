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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                            new ParentLink(diskId, AuthTarget.DISK))));
        }
        return "disk";
    }

    @RequestMapping("/space")
    public String spaceManagement() {
        return "setting/space";
    }

    @PostMapping("/save")
    public String save(@RequestParam("diskName") String diskName,
                       @RequestParam(value = "shareGroups", required = false) String[] shareGroups,
                       RedirectAttributes redirectAttributes,
                       Model model,
                       HttpSession session) {
        List<AccessAuthorization> authorizations = Arrays.asList(shareGroups).stream()
                .map(group -> new AccessAuthorization(group, AuthType.GROUP, AuthAction.READ))
                .collect(Collectors.toList());
        diskService.save(diskName, authorizations);
        return "redirect:/#disk/space";
    }

}
