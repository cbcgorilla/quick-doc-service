package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.common.annotation.PreAuth;
import cn.mxleader.quickdoc.entities.Metadata;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.web.domain.WebFile;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

public interface FileService {

    WebFile getStoredFile(ObjectId fileId);

    WebFile getStoredFile(String filename, ObjectId folderId);

    Stream<WebFile> getWebFiles(ObjectId folderId);

    @PreAuth(field = ParentLink.class)
    List<WebFile> list(ParentLink parent);

    /**
     * 根据文件名进行模糊查询
     *
     * @param filename
     * @return
     */
    Stream<WebFile> searchFilesContaining(String filename);

    ObjectId store(InputStream file, String filename, String contentType);

    ObjectId store(InputStream file, String filename, Metadata metadata);

    ObjectId storeServerFile(String resourceLocation) throws FileNotFoundException;

    void rename(ObjectId fileId, String newFilename);

    GridFSFile saveMetadata(ObjectId fileId, Metadata metadata);

    GridFSFile addParent(ObjectId fileId, ParentLink parent);

    /**
     * 删除Mongo库内文件
     *
     * @param fileId 文件ID
     * @return
     */
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
    void createZip(ObjectId folderId, OutputStream fos, SysUser activeUser) throws IOException;

}
