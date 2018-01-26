package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.web.domain.WebDirectory;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDirectoryService {

    /**
     * 新增文件目录, 默认上级目录ID为0；
     *
     * @param path
     * @param parentId
     * @param owners
     * @return
     */
    Mono<FsDirectory> saveDirectory(String path, ObjectId parentId, Boolean publicVisible, FsOwner[] owners);

    /**
     * 新增文件目录;
     *
     * @param fsDirectory
     * @return
     */
    Mono<FsDirectory> saveDirectory(FsDirectory fsDirectory);

    /**
     * 重命名文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directory
     * @param newPath
     * @return
     */
    Mono<FsDirectory> renameDirectory(FsDirectory directory, String newPath);

    /**
     * 迁移文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directoryId 待迁移文件夹ID
     * @param newParentId 新上级目录ID
     * @return
     */
    Mono<FsDirectory> moveDirectory(ObjectId directoryId, ObjectId newParentId);

    /**
     * 删除文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directoryId
     * @return
     */
    Mono<Void> deleteDirectory(ObjectId directoryId);

    /**
     * 根据上级目录ID信息获取子文件目录
     *
     * @param parentId
     * @return
     */
    Flux<WebDirectory> findAllByParentIdInWebFormat(ObjectId parentId);

    Flux<FsDirectory> findAllByParentId(ObjectId parentId);

    Flux<WebDirectory> findAllInWebFormat();

    Flux<FsDirectory> findAll();

    /**
     * 获取文件目录
     *
     * @param path     文件路径名
     * @param parentId 上级目录ID
     * @return
     */
    Mono<FsDirectory> findByPathAndParentId(String path, ObjectId parentId);

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    Mono<FsDirectory> findById(ObjectId id);

}
