package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.common.utils.FileUtils;
import cn.mxleader.quickdoc.entities.AuthTarget;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.service.FolderService;
import cn.mxleader.quickdoc.service.StreamService;
import cn.mxleader.quickdoc.web.domain.WebFile;
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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.Charset;
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

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(Model model, HttpSession session) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        return "files";
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
                          HttpSession session) throws IOException {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + folderId + ".zip");
        // @TODO 压缩包文件大小在文件下载完毕前无法获取
        // response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));

        fileService.createZip(folderId, response.getOutputStream(), activeUser);
    }

    /**
     * 文件下载： 提供文件ID , 下载文件名默认编码为GB2312.
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
                "attachment; filename=" + new String(fs.getFilename()
                        .getBytes("gb2312"), "ISO8859-1"));
        response.setHeader("Content-Length", String.valueOf(fs.contentLength()));
        FileCopyUtils.copy(fs.getInputStream(), response.getOutputStream());
    }

    /**
     * 预览文件共用方法
     *
     * @param returnType
     * @param fileId
     * @return
     * @throws IOException
     */
    private HttpEntity<byte[]> openDocumentEntity(MediaType returnType, ObjectId fileId) throws IOException {
        GridFsResource fs = fileService.getResource(fileId);
        byte[] document = FileCopyUtils.copyToByteArray(fs.getInputStream());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(returnType);
        header.set("Content-Disposition", "inline; filename=" + fs.getFilename());
        header.setContentLength(document.length);

        return new HttpEntity<>(document, header);
    }

    /**
     * PDF格式文件预览功能
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-pdf/{fileId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openPdfEntity(@PathVariable ObjectId fileId) throws IOException {
        return openDocumentEntity(MediaType.APPLICATION_PDF, fileId);
    }

    /**
     * GIF预览功能
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-gif/{fileId}", produces = MediaType.IMAGE_GIF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openImageGifEntity(@PathVariable ObjectId fileId) throws IOException {
        return openDocumentEntity(MediaType.IMAGE_GIF, fileId);
    }

    /**
     * JPEG预览功能
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-jpeg/{fileId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openImageJpegEntity(@PathVariable ObjectId fileId) throws IOException {
        return openDocumentEntity(MediaType.IMAGE_JPEG, fileId);
    }

    /**
     * PNG预览功能
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-png/{fileId}", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openImageEntity(@PathVariable ObjectId fileId) throws IOException {
        return openDocumentEntity(MediaType.IMAGE_PNG, fileId);
    }

    /**
     * 文本文件预览功能
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-text/{fileId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody
    HttpEntity<String> openTextEntity(@PathVariable ObjectId fileId) throws IOException {
        GridFsResource fs = fileService.getResource(fileId);

        HttpHeaders header = new HttpHeaders();
        header.set("Content-Disposition", "inline; filename=" + fs.getFilename());
        if (fs.getContentType().equalsIgnoreCase("text/html")) {
            header.add("Content-Type", "text/html; charset=utf-8");
            String document = FileUtils.read(fs.getInputStream());
            return new HttpEntity<>(document, header);
        } else {
            header.add("Content-Type", "text/plain; charset=gb2312");
            String document = FileUtils.read(fs.getInputStream(), Charset.forName("GBK"));
            return new HttpEntity<>(document, header);
        }
    }

    /**
     * 上传文件到指定目录和文件分类
     *
     * @param file
     * @param containerId
     * @param containerType
     * @param redirectAttributes
     * @param model
     * @param session
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("containerId") ObjectId containerId,
                         @RequestParam("containerType") AuthTarget containerType,
                         @RequestParam(value = "shareSetting", required = false) String[] shareSetting,
                         @RequestParam(value = "shareGroups", required = false) String[] shareGroups,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         HttpSession session) throws IOException {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        String filename = FileUtils.getFilename(file.getOriginalFilename());
        String fileType = FileUtils.guessMimeType(filename);
/*
        MimetypesFileTypeMap m = new MimetypesFileTypeMap();
        String fileType = m.getContentType(filename);*/

        SysFolder sysFolder = folderService.findById(containerId).get();
        if (fileService.getStoredFile(filename, containerId) != null) {
            redirectAttributes.addFlashAttribute("message",
                    "该目录： " + sysFolder.getName() + "中已存在同名文件，请核对文件信息是否重复！");
            return "redirect:/#file/folder@" + containerId;
        }
        // 鉴权检查
        /*if (checkAuthentication(sysFolder.getAuthorizations(), activeUser, WRITE_PRIVILEGE)) {
            Metadata metadata = new Metadata(fileType, folderId,
                    translateShareSetting(activeUser, shareSetting, shareGroups), null);

            ObjectId fileId = fileService.store(file.getInputStream(), filename, metadata);
            // 启动TensorFlow 线程分析图片内容
            if (fileType.startsWith("image/")) {
                tensorFlowService.updateImageMetadata(fileId, metadata);
            }

            refreshDirList(model, folderId);
            // 发送MQ消息
            streamService.sendMessage("用户" + activeUser.getUsername() +
                    "成功上传文件： " + filename + "到目录：" + folderId);
            redirectAttributes.addFlashAttribute("message",
                    "成功上传文件： " + filename);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无此目录的上传权限： " + sysFolder.getName() + "，请联系管理员获取！");
        }*/
        return "redirect:/#files/folder@" + containerId;
    }

    /**
     * 删除文件
     *
     * @param folderId
     * @param fileId
     * @return
     */
    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestParam("folderId") ObjectId folderId,
                             @RequestParam("fileId") ObjectId fileId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
        WebFile file = fileService.getStoredFile(fileId);
        /*if (checkAuthentication(file.getAuthorizations(), activeUser, DELETE_PRIVILEGE)) {
            fileService.delete(fileId);
            redirectAttributes.addFlashAttribute("message",
                    "成功删除文件： " + file.getFilename());
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除此文件的权限： " + file.getFilename() + "，请联系管理员获取！");
        }*/
        return "redirect:/#files/folder@" + folderId;
    }

}
