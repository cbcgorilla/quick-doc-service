package com.neofinance.quickdoc.web.mvc;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.common.entities.FsEntity;
import com.neofinance.quickdoc.common.utils.KeyUtil;
import com.neofinance.quickdoc.service.ReactiveCategoryService;
import com.neofinance.quickdoc.service.ReactiveDirectoryService;
import com.neofinance.quickdoc.service.ReactiveFileService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class IndexController {

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

    @GetMapping()
    public String index(Model model) throws IOException {
        model.addAttribute("message", "文件服务器：强大源自共享");

        model.addAttribute("directories",
                reactiveDirectoryFsService.allRootDirectories()
                        .toStream()
                        .collect(Collectors.toList()));
        return "index";
    }

    @GetMapping("/{directoryId}")
    public String index(@PathVariable Long directoryId, Model model) throws IOException {
        model.addAttribute("message", "文件服务器：强大源自共享：");

        model.addAttribute("directories",
                reactiveDirectoryFsService.findAllByParentId(directoryId)
                        .toStream()
                        .collect(Collectors.toList()));
        model.addAttribute("files",
                reactiveFileService.getStoredFiles(directoryId)
                        .toStream()
                        .collect(Collectors.toList()));

        model.addAttribute("directoryId", directoryId);

        return "index";
    }

    @GetMapping(value = "/view/{storedId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openDocumentInBrowser(@PathVariable("storedId") String storedId) throws IOException {
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
                                   Model model) throws IOException {

        FsEntity fsEntity = new FsEntity(KeyUtil.getSHA256UUID(),
                file.getOriginalFilename(),
                file.getSize(),
                "PDF",
                new Date(),
                1503903557505L,
                directoryId,
                null,
                null);
        reactiveFileService.storeFile(
                fsEntity,
                file.getInputStream())
                .subscribe();
        redirectAttributes.addFlashAttribute("message", "成功上传文件： " + file.getOriginalFilename());

        return "redirect:/"+directoryId;
    }

}
