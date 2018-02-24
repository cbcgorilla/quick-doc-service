package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.common.utils.FileUtils;
import cn.mxleader.quickdoc.entities.FileMetadata;
import cn.mxleader.quickdoc.entities.QuickDocFolder;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.service.ReactiveFolderService;
import cn.mxleader.quickdoc.service.StreamService;
import cn.mxleader.quickdoc.web.domain.WebFile;
import cn.mxleader.quickdoc.web.domain.WebFolder;
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
import java.util.List;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;
import static cn.mxleader.quickdoc.web.config.AuthenticationToolkit.*;
import static cn.mxleader.quickdoc.web.config.WebHandlerInterceptor.FILES_ATTRIBUTE;
import static cn.mxleader.quickdoc.web.config.WebHandlerInterceptor.FOLDERS_ATTRIBUTE;

@Controller
@RequestMapping("/")
@SessionAttributes("ActiveUser")
public class IndexController {

    private final ReactiveFolderService reactiveFolderService;
    private final FileService fileService;
    private final ConfigService configService;
    private final StreamService streamService;

    @Autowired
    public IndexController(ReactiveFolderService reactiveFolderService,
                           FileService fileService,
                           ConfigService configService,
                           StreamService streamService) {
        this.reactiveFolderService = reactiveFolderService;
        this.fileService = fileService;
        this.configService = configService;
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
        ObjectId rootParentId = configService.getQuickDocHealth().getId();
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        model.addAttribute("isAdmin", activeUser.isAdmin());
        if (activeUser.isAdmin()) {
            refreshDirList(model, rootParentId);
        } else {
            List<WebFolder> webFolders = reactiveFolderService.findAllByParentIdInWebFormat(rootParentId)
                    .filter(webFolder -> webFolder.getPath().equalsIgnoreCase("root"))
                    .toStream()
                    .collect(Collectors.toList());
            if (webFolders != null && webFolders.size() > 0) {
                for (WebFolder subFolder : webFolders) {
                    model.addAttribute("currentFolder", subFolder);
                    refreshDirList(model, subFolder.getId());
                }
            }
        }
        return "index";
    }

    /**
     * 刷新显示指定文件夹内的所有内容
     *
     * @param folderId
     * @param model
     * @return
     */
    @GetMapping("/folder@{folderId}")
    public String index(@PathVariable ObjectId folderId, Model model, HttpSession session) {
        QuickDocFolder quickDocFolder = reactiveFolderService.findById(folderId).block();
        model.addAttribute("currentFolder", quickDocFolder);
        refreshDirList(model, folderId);
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        model.addAttribute("isAdmin", activeUser.isAdmin());

        return "index";
    }

    /**
     * 刷新文件夹目录内容
     *
     * @param model
     * @param folderId
     */
    private void refreshDirList(Model model, ObjectId folderId) {
        model.addAttribute(FOLDERS_ATTRIBUTE,
                reactiveFolderService.findAllByParentIdInWebFormat(folderId)
                        .toStream().collect(Collectors.toList()));
        model.addAttribute(FILES_ATTRIBUTE, fileService.getWebFiles(folderId)
                .collect(Collectors.toList()));
    }

    @RequestMapping("/search")
    public String searchFiles(@RequestParam("filename") String filename,
                              Model model, HttpSession session) {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        model.addAttribute("isAdmin", activeUser.isAdmin());
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
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
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
    private HttpEntity<byte[]> openDocumentEntity(MediaType returnType, String fileId) throws IOException {
        GridFsResource fs = fileService.getResource(new ObjectId(fileId));
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
    HttpEntity<byte[]> openPdfEntity(@PathVariable String fileId) throws IOException {
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
    HttpEntity<byte[]> openImageGifEntity(@PathVariable String fileId) throws IOException {
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
    HttpEntity<byte[]> openImageJpegEntity(@PathVariable String fileId) throws IOException {
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
    HttpEntity<byte[]> openImageEntity(@PathVariable String fileId) throws IOException {
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
    HttpEntity<String> openTextEntity(@PathVariable String fileId) throws IOException {
        GridFsResource fs = fileService.getResource(new ObjectId(fileId));

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
     * @param folderId
     * @param redirectAttributes
     * @param model
     * @param session
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("folderId") ObjectId folderId,
                         @RequestParam(value = "owners", required = false) String[] ownersRequest,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         HttpSession session) throws IOException {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        String filename = FileUtils.getFilename(file.getOriginalFilename());
        String fileType = FileUtils.guessMimeType(filename);

        QuickDocFolder quickDocFolder = reactiveFolderService.findById(folderId).block();
        if (fileService.getStoredFile(filename, folderId) != null) {
            redirectAttributes.addFlashAttribute("message",
                    "该目录： " + quickDocFolder.getPath() + "中已存在同名文件，请核对文件信息是否重复！");
            return "redirect:/folder@" + folderId;
        }
        // 鉴权检查
        if (checkAuthentication(quickDocFolder.getOpenAccess(),
                quickDocFolder.getAuthorizations(), activeUser, WRITE_PRIVILEGE)) {
            FileMetadata metadata = new FileMetadata(fileType, folderId,
                    getOpenAccessFromOwnerRequest(ownersRequest),
                    translateOwnerRequest(activeUser, ownersRequest), null);

            ObjectId fileId = fileService.store(file.getInputStream(), filename, metadata);
            // 启动TensorFlow 线程分析图片内容
            /*if (fileType.startsWith("image/")) {
                tensorFlowService.updateImageMetadata(fileId, metadata);
            }*/

            refreshDirList(model, folderId);
            // 发送MQ消息
            streamService.sendMessage("用户" + activeUser.getUsername() +
                    "成功上传文件： " + filename + "到目录：" + folderId);
            redirectAttributes.addFlashAttribute("message",
                    "成功上传文件： " + filename);
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无此目录的上传权限： " + quickDocFolder.getPath() + "，请联系管理员获取！");
        }
        return "redirect:/folder@" + folderId;
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
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        WebFile file = fileService.getStoredFile(fileId);
        if (checkAuthentication(file.getOpenAccess(), file.getAuthorizations(), activeUser, DELETE_PRIVILEGE)) {
            fileService.delete(fileId);
            redirectAttributes.addFlashAttribute("message",
                    "成功删除文件： " + file.getFilename());
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "您无删除此文件的权限： " + file.getFilename() + "，请联系管理员获取！");
        }
        return "redirect:/folder@" + folderId;
    }

}
