package cn.techfan.quickdoc.web.mvc;

import cn.techfan.quickdoc.common.entities.FsCategory;
import cn.techfan.quickdoc.common.entities.FsDirectory;
import cn.techfan.quickdoc.common.entities.FsEntity;
import cn.techfan.quickdoc.common.entities.FsOwner;
import cn.techfan.quickdoc.common.utils.KeyUtil;
import cn.techfan.quickdoc.common.utils.StringUtil;
import cn.techfan.quickdoc.service.ReactiveDirectoryService;
import cn.techfan.quickdoc.service.ReactiveFileService;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import cn.techfan.quickdoc.security.ActiveUser;
import cn.techfan.quickdoc.service.ReactiveCategoryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@SessionAttributes("StorePath")
public class IndexController {

    private static final String HOME_TITLE = "极速云存储（Quick Sharing）";

    private final ReactiveCategoryService reactiveCategoryService;
    private final ReactiveDirectoryService reactiveDirectoryFsService;
    private final ReactiveFileService reactiveFileService;

    @Autowired
    public IndexController(ReactiveCategoryService reactiveCategoryService,
                           ReactiveDirectoryService reactiveDirectoryFsService,
                           ReactiveFileService reactiveFileService) {
        this.reactiveCategoryService = reactiveCategoryService;
        this.reactiveDirectoryFsService = reactiveDirectoryFsService;
        this.reactiveFileService = reactiveFileService;
    }

    @ModelAttribute("allCategories")
    public List<FsCategory> allCategories() {
        return reactiveCategoryService.findAll().toStream().collect(Collectors.toList());
    }

    @ModelAttribute("title")
    public String pageTitle() {
        return HOME_TITLE;
    }

    @GetMapping()
    public String index(Model model) {
        refreshDirList(model, 0L);
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/{directoryId}")
    public String index(@PathVariable Long directoryId, Model model) {
        FsDirectory directoryMono = reactiveDirectoryFsService.findById(directoryId).block();
        model.addAttribute("currentdirectory", directoryMono);
        refreshDirList(model, directoryId);

        return "index";
    }

    @GetMapping(value = "/zip/{directoryId}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    void downloadDocument(HttpServletResponse response,
                          @PathVariable Long directoryId) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + directoryId + ".zip");
        //response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));

        reactiveFileService.createZip(directoryId, response.getOutputStream(),0L);
    }

    @GetMapping(value = "/view/{storedId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openDocumentInBrowser(@PathVariable String storedId) throws IOException {
        GridFSDownloadStream fs = reactiveFileService.getFileStream(new ObjectId(storedId));
        byte[] document = FileCopyUtils.copyToByteArray(fs);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "pdf"));
        header.set("Content-Disposition", "inline; filename=" + fs.getGridFSFile().getFilename());
        header.setContentLength(document.length);

        return new HttpEntity<byte[]>(document, header);
    }

    @PostMapping()
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("directoryId") Long directoryId,
                                   @RequestParam("categoryId") Long categoryId,
                                   RedirectAttributes redirectAttributes,
                                   Model model,
                                   HttpSession session) throws IOException {
        ActiveUser activeUser = (ActiveUser) session.getAttribute("ActiveUser");
        FsOwner owner = new FsOwner(activeUser.getUsername(), FsOwner.Type.TYPE_PRIVATE,5);
        String filename = StringUtil.getFilename(file.getOriginalFilename());

        FsEntity fsEntity = new FsEntity(KeyUtil.getSHA256UUID(),
                filename,
                file.getSize(),
                "PDF", //@TODO 修改为实际文件类型
                new Date(),
                categoryId,
                directoryId,
                null,
                new FsOwner[]{owner},
                null,
                null);
        reactiveFileService.storeFile(
                fsEntity,
                file.getInputStream())
                .subscribe();
        refreshDirList(model, directoryId);
        redirectAttributes.addFlashAttribute("message", "成功上传文件： " + filename);
        return "redirect:/" + directoryId;
    }

    private void refreshDirList(Model model, Long directoryId) {
        model.addAttribute("directories",
                reactiveDirectoryFsService.findAllByParentId(directoryId)
                        .toStream()
                        .collect(Collectors.toList()));
        model.addAttribute("files",
                reactiveFileService.getStoredFiles(directoryId)
                        .toStream()
                        .collect(Collectors.toList()));
    }

}
