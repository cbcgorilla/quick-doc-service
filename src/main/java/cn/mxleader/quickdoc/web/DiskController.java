package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysDisk;
import cn.mxleader.quickdoc.service.DiskService;
import cn.mxleader.quickdoc.service.FolderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
                            new ParentLink(diskId, AuthTarget.DISK))));
        }
        return "disk";
    }

}
