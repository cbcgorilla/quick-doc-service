package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.security.session.ActiveUser;
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
    Mono<FsDetail> getStoredFile(String filename, ObjectId directoryId);

    Mono<FsDetail> getStoredFile(ObjectId fsDetailId);

    /**
     * 枚举目录内的所有文件
     *
     * @param directoryId 所在目录ID
     * @return
     */
    Flux<FsDetail> getStoredFiles(ObjectId directoryId);

    /**
     * 存储文件， 如同名文件已存在则更新文件内容
     *
     * @param fsDetail 文件描述信息
     * @param file     文件二进制流
     * @return
     */
    Mono<FsDetail> storeFile(FsDetail fsDetail, InputStream file);

    /**
     * 删除Mongo库内文件
     *
     * @param fsDetail 文件信息
     * @return
     */
    Mono<Void> deleteFile(FsDetail fsDetail);

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
