package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.service.StreamService;
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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;
import static cn.mxleader.quickdoc.web.config.WebHandlerInterceptor.FILES_ATTRIBUTE;

@Controller
@RequestMapping("/file")
@SessionAttributes("ActiveUser")
public class FileController {

    private final FolderService folderService;
    private final FileService fileService;
    private final StreamService streamService;

    @Autowired
    public FileController(FolderService folderService,
                          FileService fileService,
                          StreamService streamService) {
        this.folderService = folderService;
        this.fileService = fileService;
        this.streamService = streamService;
    }

    @RequestMapping("/search")
    public String searchFiles(@RequestParam("filename") String filename,
                              Model model, HttpSession session) {
        model.addAttribute(FILES_ATTRIBUTE, fileService.searchFilesContaining(filename)
                .collect(Collectors.toList()));
        return "search";
    }

    /**
     * 打包下载指定文件夹内的所有内容
     *
     * @param response
     * @param folderId
     * @throws IOException
     */
    @GetMapping(value = "/zip-resource/{folderId}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    void downloadDocument(HttpServletResponse response,
                          @PathVariable ObjectId folderId,
                          @SessionAttribute(SESSION_USER) SysUser activeUser) throws IOException {
        response.setHeader("Content-Disposition",
                "attachment; filename=" + folderId + ".zip");
        // @TODO 压缩包文件大小在文件下载完毕前无法获取
        // response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));

        fileService.createZip(folderId, response.getOutputStream());
    }

    @GetMapping(value = "/zip-package", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    void downloadZipPackage(HttpServletResponse response,
                            @RequestParam String parent,
                            @RequestParam ObjectId[] ids) throws IOException {
        response.setHeader("Content-Disposition",
                "attachment; filename=" + java.net.URLEncoder.encode(parent + ".zip", "UTF-8"));
        // @TODO 压缩包文件大小在文件下载完毕前无法获取
        // response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));

        fileService.createZipFromList(ids, response.getOutputStream(), parent);
    }

    /**
     * 文件下载： 提供文件ID
     *
     * @param response
     * @param fileId   文件存储ID号
     * @throws IOException
     */
    @GetMapping(value = "/download/{fileId}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    void downloadDocument(HttpServletResponse response,
                          @PathVariable String fileId) throws IOException {
        GridFsResource fs = fileService.getResource(new ObjectId(fileId));

        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + java.net.URLEncoder.encode(fs.getFilename(), "UTF-8"));
        response.setHeader("Content-Length", String.valueOf(fs.contentLength()));
        FileCopyUtils.copy(fs.getInputStream(), response.getOutputStream());
    }

    /**
     * 预览文件共用方法
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/preview/{fileId}")
    public @ResponseBody
    HttpEntity<byte[]> previewDocument(@PathVariable ObjectId fileId) throws IOException {
        GridFsResource fs = fileService.getResource(fileId);
        byte[] document = FileCopyUtils.copyToByteArray(fs.getInputStream());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.valueOf(fs.getContentType()));
        header.set("Content-Disposition",
                "inline; filename=" + java.net.URLEncoder.encode(fs.getFilename(), "UTF-8"));
        header.setContentLength(document.length);

        return new HttpEntity<>(document, header);
    }

}
