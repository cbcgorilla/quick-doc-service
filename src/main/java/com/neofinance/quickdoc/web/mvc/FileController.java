package com.neofinance.quickdoc.web.mvc;

import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.service.CategoryService;
import com.neofinance.quickdoc.service.GridFsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/files")
public class FileController {

    public static final String LOCAL_TEMP_DIR = "E:\\temp\\";

    private final CategoryService categoryService;
    private final GridFsService gridFsService;

    @Autowired
    public FileController(CategoryService categoryService, GridFsService gridFsService) {
        this.categoryService = categoryService;
        this.gridFsService = gridFsService;
    }

    @ModelAttribute("allCategories")
    public List<FsCategory> allCategories() {
        return categoryService.findAll().toStream().collect(Collectors.toList());
    }

    @GetMapping()
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("countoffiles",
                "服务端文件总数：" + gridFsService.loadAllFilenames().count());


        model.addAttribute("files", gridFsService.loadAllFilenames().map(
                path -> "pdf/" + path)
                .collect(Collectors.toList()));
        /*
        model.addAttribute("files", gridFsService.loadAllFilenames().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileController.class,
                        "serveFile", path.toString()).build().toString())
                .collect(Collectors.toList()));
*/
        return "uploadForm";
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        InputStream in = gridFsService.getFileResource(filename).getInputStream();
        Resource file = new FileSystemResource(getTempResourceFile(in, LOCAL_TEMP_DIR + filename));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"").body(file);
    }

    @PostMapping()
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws IOException {

        gridFsService.storeFile(file.getInputStream(), file.getOriginalFilename(), "");
        redirectAttributes.addFlashAttribute("message", "成功上传文件： " + file.getOriginalFilename());
        return "redirect:/files/";
    }

    @GetMapping(value = "/download/{filename:.+}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    void downloadDocument(HttpServletResponse response,
                          @PathVariable("filename") String filename) throws IOException {
        GridFsResource fsResource = gridFsService.getFileResource(filename);
        InputStream in = fsResource.getInputStream();

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));
        FileCopyUtils.copy(in, response.getOutputStream());
    }

    @GetMapping(value = "/pdf/{filename:.+}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openDocumentInBrowser(@PathVariable("filename") String filename) throws IOException {
        InputStream in = gridFsService.getFileResource(filename).getInputStream();
        byte[] document = FileCopyUtils.copyToByteArray(in);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "pdf"));
        header.set("Content-Disposition", "inline; filename=" + filename);
        header.setContentLength(document.length);

        return new HttpEntity<byte[]>(document, header);
    }

    /**
     * 同名文件多并发请求时有冲突可能, 占用服务端本地缓存目录，需要定期清理Web服务器缓存目录
     *
     * @param response
     * @param filename
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/resource/{filename:.+}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    Resource resourceDownload(HttpServletResponse response,
                              @PathVariable("filename") String filename) throws IOException {
        GridFsResource fsResource = gridFsService.getFileResource(filename);
        InputStream in = fsResource.getInputStream();

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "inline; filename=" + filename);
        response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));
        return new FileSystemResource(getTempResourceFile(in, LOCAL_TEMP_DIR + filename));
    }


/*

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ApiResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ApiResponseEntity.notFound().build();
    }
*/

    private File getTempResourceFile(InputStream in, String tempFilename) {
        try {
            File f = new File(tempFilename);
            FileOutputStream out = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();
            in.close();
            return f;
        } catch (IOException e) {
            return null;
        }
    }

}