package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.security.authorization.PreAuth;
import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.web.domain.WebFile;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

public interface FileService {

    WebFile getStoredFile(ObjectId fileId);

    @PreAuth(field = ParentLink.class)
    WebFile getStoredFile(String filename, ParentLink parent);

    Stream<WebFile> getWebFiles(ObjectId folderId);

    @PreAuth(field = ParentLink.class)
    List<WebFile> list(ParentLink parent);

    @PreAuth(field = ParentLink.class)
    Page<WebFile> list(ParentLink parent, Pageable pageable);

    /**
     * 根据文件名进行模糊查询
     *
     * @param filename
     * @return
     */
    Stream<WebFile> searchFilesContaining(String filename);

    @PreAuth(field = ParentLink.class, action = AuthAction.WRITE)
    ObjectId store(InputStream file, String filename, ParentLink parent);

    ObjectId storeServerFile(String resourceLocation) throws IOException;

    void rename(ObjectId fileId, String newFilename);

    @PreAuth(target = AuthTarget.FILE, action = AuthAction.WRITE)
    GridFSFile saveMetadata(ObjectId fileId, Metadata metadata);

    GridFSFile addParent(ObjectId fileId, ParentLink parent);

    GridFSFile addAuthorization(ObjectId fileId, Authorization authorization);
    /**
     * 删除Mongo库内文件
     *
     * @param fileId 文件ID
     * @return
     */
    @PreAuth(target = AuthTarget.FILE, action = AuthAction.DELETE)
    void delete(ObjectId fileId);

    /**
     * 根据输入文件ID获取二进制流
     *
     * @param fileId 文件ID
     * @return
     */
    GridFsResource getResource(ObjectId fileId);

    GridFSDownloadStream getFSDownloadStream(ObjectId fileId);

    /**
     * 创建ZIP文件
     *
     * @param folderId   文件或文件夹路径
     * @param fos        生成的zip文件存在路径（包括文件名）
     * @param activeUser 用户信息
     */
    @PreAuth
    void createZip(ObjectId folderId, OutputStream fos, SysUser activeUser) throws IOException;
    //@PreAuth
    void createZipFromList(String[] ids, OutputStream fos, String parent) throws IOException ;
}
