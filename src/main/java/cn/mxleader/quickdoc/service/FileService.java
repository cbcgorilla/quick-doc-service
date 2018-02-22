package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FileMetadata;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import cn.mxleader.quickdoc.web.domain.WebFile;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

public interface FileService {

    WebFile getStoredFile(ObjectId fileId);

    WebFile getStoredFile(String filename, ObjectId folderId);

    Stream<WebFile> getWebFiles(ObjectId folderId);


    /**
     * 根据文件名进行模糊查询
     *
     * @param filename
     * @return
     */
    Stream<WebFile> searchFilesContaining(String filename);

    ObjectId store(InputStream file, String filename, String contentType);

    ObjectId store(InputStream file, String filename, FileMetadata metadata);

    void rename(ObjectId fileId, String newFilename);

    GridFSFile saveMetadata(ObjectId fileId, FileMetadata fileMetadata);

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
     * @param storedId 文件ID
     * @return
     */
    GridFSDownloadStream getFileStream(ObjectId storedId);

    /**
     * 创建ZIP文件
     *
     * @param folderId   文件或文件夹路径
     * @param fos        生成的zip文件存在路径（包括文件名）
     * @param activeUser 用户信息
     */
    void createZip(ObjectId folderId, OutputStream fos, ActiveUser activeUser) throws IOException;

}