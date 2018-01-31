package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FsDescription;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ReactiveFileService {

    /**
     * 在指定目录内查找相应名字的文件
     *
     * @param filename    输入文件名
     * @param directoryId 所在目录ID
     * @return
     */
    Mono<FsDescription> getStoredFile(String filename, ObjectId directoryId);

    Mono<FsDescription> getStoredFile(ObjectId fsDetailId);

    /**
     * 枚举目录内的所有文件
     *
     * @param directoryId 所在目录ID
     * @return
     */
    Flux<FsDescription> getStoredFiles(ObjectId directoryId);

    /**
     * 根据文件名进行模糊查询
     *
     * @param filename
     * @return
     */
    Flux<FsDescription> getStoredFilesNameContaining(String filename);

    /**
     * 存储文件， 如同名文件已存在则更新文件内容
     *
     * @param fsDescription 文件描述信息
     * @param file          文件二进制流
     * @return
     */
    Mono<FsDescription> storeFile(FsDescription fsDescription, InputStream file);
    Mono<FsDescription> updateFsDescription(FsDescription fsDescription);

    /**
     * 删除Mongo库内文件
     *
     * @param fsDescription 文件信息
     * @return
     */
    Mono<Void> deleteFile(FsDescription fsDescription);

    /**
     * 删除Mongo库内文件
     *
     * @param fsDetailId 文件ID信息
     * @return
     */
    Mono<Void> deleteFile(ObjectId fsDetailId);

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
     * @param directoryId 文件或文件夹路径
     * @param fos         生成的zip文件存在路径（包括文件名）
     * @param categoryId  待压缩的文件分类ID，为0L则压缩所有分类
     */
    void createZip(ObjectId directoryId, OutputStream fos,
                   ObjectId categoryId, ActiveUser activeUser) throws IOException;

}
