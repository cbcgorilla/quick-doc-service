package com.neofinance.quickdoc.web.mvc;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.common.entities.FsDirectory;
import com.neofinance.quickdoc.common.entities.FsEntity;
import com.neofinance.quickdoc.common.entities.FsOwner;
import com.neofinance.quickdoc.common.utils.KeyUtil;
import com.neofinance.quickdoc.security.ActiveUser;
import com.neofinance.quickdoc.service.ReactiveCategoryService;
import com.neofinance.quickdoc.service.ReactiveDirectoryService;
import com.neofinance.quickdoc.service.ReactiveFileService;
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

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
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
        FsOwner owner = new FsOwner(activeUser.getUsername(), FsOwner.Type.TYPE_PRIVATE);

        FsEntity fsEntity = new FsEntity(KeyUtil.getSHA256UUID(),
                file.getOriginalFilename(),
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
        redirectAttributes.addFlashAttribute("message", "成功上传文件： " + file.getOriginalFilename());
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
