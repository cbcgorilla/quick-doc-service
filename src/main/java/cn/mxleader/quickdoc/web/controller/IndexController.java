package cn.mxleader.quickdoc.web.controller;

import cn.mxleader.quickdoc.common.utils.StringUtil;
import cn.mxleader.quickdoc.entities.FsCategory;
import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.security.model.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.service.ReactiveFileService;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.utils.KeyUtil.getSHA256UUID;
import static cn.mxleader.quickdoc.common.utils.StringUtil.HOME_TITLE;

@Controller
@RequestMapping("/")
@SessionAttributes("ActiveUser")
public class IndexController {

    private static final String SESSION_USER = "ActiveUser";

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

    /**
     * 获取所有文件分类清单
     *
     * @return
     */
    @ModelAttribute("allCategories")
    public List<FsCategory> allCategories() {
        return reactiveCategoryService.findAll().toStream().collect(Collectors.toList());
    }

    /**
     * 获取系统标题
     *
     * @return
     */
    @ModelAttribute("title")
    public String pageTitle() {
        return HOME_TITLE;
    }

    /**
     * 登录后的首页
     *
     * @param model
     * @return
     */
    @GetMapping()
    public String index(Model model, HttpSession session) {
        refreshDirList(model, 0L, session);
        return "index";
    }

    /**
     * 刷新显示指定文件夹内的所有内容
     *
     * @param directoryId
     * @param model
     * @return
     */
    @GetMapping("/path@{directoryId}")
    public String index(@PathVariable Long directoryId, Model model, HttpSession session) {
        FsDirectory directoryMono = reactiveDirectoryFsService.findById(directoryId).block();
        model.addAttribute("currentdirectory", directoryMono);
        refreshDirList(model, directoryId, session);

        return "index";
    }

    /**
     * 打包下载指定文件夹内的所有内容
     *
     * @param response
     * @param directoryId
     * @throws IOException
     */
    @GetMapping(value = "/zip-resource/{directoryId}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    void downloadDocument(HttpServletResponse response,
                          @PathVariable Long directoryId) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + directoryId + ".zip");
        //response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));

        reactiveFileService.createZip(directoryId, response.getOutputStream(), 0L);
    }

    /**
     * 文件下载： 提供文件ID , 下载文件名默认编码为GB2312.
     *
     * @param response
     * @param storedId 文件存储ID号
     * @throws IOException
     */
    @GetMapping(value = "/download/{storedId}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    void downloadDocument(HttpServletResponse response,
                          @PathVariable String storedId) throws IOException {
        GridFSDownloadStream fs = reactiveFileService.getFileStream(new ObjectId(storedId));
        GridFSFile gridFSFile = fs.getGridFSFile();

        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + new String(gridFSFile.getFilename()
                        .getBytes("gb2312"), "ISO8859-1"));
        response.setHeader("Content-Length", String.valueOf(gridFSFile.getLength()));
        FileCopyUtils.copy(fs, response.getOutputStream());
    }

    /**
     * PDF格式文件预览功能
     *
     * @param storedId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-pdf/{storedId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openPdfEntity(@PathVariable String storedId) throws IOException {
        return openDocumentEntity(MediaType.APPLICATION_PDF, storedId);
    }

    /**
     * GIF预览功能
     *
     * @param storedId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-gif/{storedId}", produces = MediaType.IMAGE_GIF_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openImageGifEntity(@PathVariable String storedId) throws IOException {
        return openDocumentEntity(MediaType.IMAGE_GIF, storedId);
    }

    /**
     * JPEG预览功能
     *
     * @param storedId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-jpeg/{storedId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openImageJpegEntity(@PathVariable String storedId) throws IOException {
        return openDocumentEntity(MediaType.IMAGE_JPEG, storedId);
    }

    /**
     * PNG预览功能
     *
     * @param storedId
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/view-png/{storedId}", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> openImageEntity(@PathVariable String storedId) throws IOException {
        return openDocumentEntity(MediaType.IMAGE_PNG, storedId);
    }

    private HttpEntity<byte[]> openDocumentEntity(MediaType returnType, String storedId) throws IOException {
        GridFSDownloadStream fs = reactiveFileService.getFileStream(new ObjectId(storedId));
        byte[] document = FileCopyUtils.copyToByteArray(fs);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(returnType);
        header.set("Content-Disposition", "inline; filename=" + fs.getGridFSFile().getFilename());
        header.setContentLength(document.length);

        return new HttpEntity<byte[]>(document, header);
    }

    /**
     * 上传文件到指定目录和文件分类
     *
     * @param file
     * @param directoryId
     * @param categoryId
     * @param redirectAttributes
     * @param model
     * @param session
     * @return
     * @throws IOException
     */
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("directoryId") Long directoryId,
                             @RequestParam("categoryId") Long categoryId,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) throws IOException {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        FsOwner owner = new FsOwner(activeUser.getUsername(), FsOwner.Type.TYPE_PRIVATE, 5);
        String filename = StringUtil.getFilename(file.getOriginalFilename());
        FsDetail fsDetail = new FsDetail(getSHA256UUID(),
                filename,
                file.getSize(),
                StringUtils.getFilenameExtension(filename).toLowerCase(),
                new Date(),
                categoryId,
                directoryId,
                null,
                new FsOwner[]{owner},
                null,
                null);
        reactiveFileService.storeFile(
                fsDetail,
                file.getInputStream())
                .subscribe();
        refreshDirList(model, directoryId, session);
        redirectAttributes.addFlashAttribute("message", "成功上传文件： " + filename);
        return "redirect:/path@" + directoryId;
    }

    /**
     * 删除文件
     *
     * @param directoryId
     * @param fsEntityId
     * @return
     */
    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestParam("directoryId") Long directoryId,
                             @RequestParam String fsEntityId,
                             RedirectAttributes redirectAttributes) {
        reactiveFileService.deleteFile(fsEntityId).subscribe();
        redirectAttributes.addFlashAttribute("message", "成功删除文件： " + fsEntityId);
        return "redirect:/path@" + directoryId;
    }

    /**
     * 刷新文件夹目录内容
     *
     * @param model
     * @param directoryId
     */
    private void refreshDirList(Model model, Long directoryId,
                                HttpSession session) {
        ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
        model.addAttribute("directories",
                reactiveDirectoryFsService.findAllByParentId(directoryId)
                        .filter(webDirectory -> {
                            return webDirectory.getOwners() != null;
                        })
                        .filter(webDirectory -> {
                            return checkAuthentication(webDirectory.getOwners(),
                                    activeUser.getGroup(), activeUser.getUsername());
                        })
                        .toStream()
                        .collect(Collectors.toList()));
        model.addAttribute("files",
                reactiveFileService.getStoredFiles(directoryId)
                        .filter(fsDetail -> {
                            return fsDetail.getOwners() != null;
                        })
                        .filter(fsDetail -> {
                            return checkAuthentication(fsDetail.getOwners(),
                                    activeUser.getGroup(), activeUser.getUsername());
                        })
                        .toStream()
                        .collect(Collectors.toList()));
    }

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param owners
     * @return
     */
    private Boolean checkAuthentication(FsOwner[] owners, String groupName, String username) {
        if (owners.length > 0) {
            for (FsOwner owner : owners) {
                int read = owner.getPrivilege() & 1;
                if (read > 0) {
                    switch (owner.getType()) {
                        case TYPE_PUBLIC:
                            return true;
                        case TYPE_GROUP:
                            if (owner.getName().equalsIgnoreCase(groupName)) {
                                return true;
                            } else {
                                break;
                            }
                        case TYPE_PRIVATE:
                            if (owner.getName().equalsIgnoreCase(username)) {
                                return true;
                            } else {
                                break;
                            }
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 同名文件多并发请求时有冲突可能, 占用服务端本地缓存目录，需要定期清理Web服务器缓存目录
     *
     * @param response
     * @param filename
     * @return
     * @throws IOException
     *//*
    @GetMapping(value = "/resource/{filename:.+}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    Resource resourceDownload(HttpServletResponse response,
                              @PathVariable("filename") String filename) throws IOException {
        GridFsResource fsResource = reactiveFileService.getFileStream(new ObjectId(filename));
        InputStream in = fsResource.getInputStream();

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "inline; filename=" + filename);
        response.setHeader("Content-Length", String.valueOf(fsResource.contentLength()));
        return new FileSystemResource(getTempResourceFile(in, LOCAL_TEMP_DIR + filename));
    }
    *//*
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
    }*/

}
