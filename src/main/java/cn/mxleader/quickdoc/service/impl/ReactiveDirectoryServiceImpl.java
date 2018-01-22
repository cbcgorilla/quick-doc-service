package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.ReactiveDirectoryRepository;
import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.web.domain.WebDirectory;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static cn.mxleader.quickdoc.common.utils.AuthenticationUtil.SYSTEM_ADMIN_GROUP_OWNER;
import static cn.mxleader.quickdoc.common.utils.MessageUtil.*;

@Service
public class ReactiveDirectoryServiceImpl implements ReactiveDirectoryService {

    private final ReactiveDirectoryRepository reactiveDirectoryRepository;
    private final MongoTemplate mongoTemplate;

    ReactiveDirectoryServiceImpl(ReactiveDirectoryRepository reactiveDirectoryRepository,
                                 MongoTemplate mongoTemplate) {
        this.reactiveDirectoryRepository = reactiveDirectoryRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 新增文件目录, 默认上级目录ID为0；
     *
     * @param path
     * @param parentId
     * @param owners
     * @return
     */
    public Mono<FsDirectory> saveDirectory(String path, ObjectId parentId,
                                           Boolean publicVisible,FsOwner[] owners) {
        return reactiveDirectoryRepository.findByPathAndParentId(path, parentId)
                .defaultIfEmpty(new FsDirectory(ObjectId.get(), path, parentId, publicVisible,owners))
                .flatMap(entity -> {
                    if (owners != null && owners.length > 0) {
                        entity.setOwners(owners);
                    } else {
                        FsOwner[] adminOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                        entity.setOwners(adminOwners);
                    }
                    return reactiveDirectoryRepository.save(entity);
                });
    }

    /**
     * 新增文件目录;
     *
     * @param fsDirectory
     * @return
     */
    public Mono<FsDirectory> saveDirectory(FsDirectory fsDirectory) {
        return saveDirectory(fsDirectory.getPath(),
                fsDirectory.getParentId(),
                fsDirectory.getPublicVisible(),
                fsDirectory.getOwners());
    }

    /**
     * 重命名文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directory
     * @param newPath
     * @return
     */
    public Mono<FsDirectory> renameDirectory(FsDirectory directory, String newPath) {
        return reactiveDirectoryRepository.findById(directory.getId())
                .switchIfEmpty(noDirectoryMsg(directory.toString()))
                .flatMap(v -> {
                    if (reactiveDirectoryRepository.findByPathAndParentId(newPath,
                            directory.getParentId()).blockOptional().isPresent()) {
                        return dirConflictMsg(newPath);
                    }
                    v.setPath(newPath);
                    return reactiveDirectoryRepository.save(v);
                });
    }

    /**
     * 迁移文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directory    待迁移文件夹
     * @param newDirectory 新上级目录
     * @return
     */
    public Mono<FsDirectory> moveDirectory(FsDirectory directory, FsDirectory newDirectory) {
        return moveDirectory(directory, newDirectory.getId());
    }

    /**
     * 迁移文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directory      待迁移文件夹
     * @param newDirectoryId 新上级目录ID
     * @return
     */
    public Mono<FsDirectory> moveDirectory(FsDirectory directory, ObjectId newDirectoryId) {
        return reactiveDirectoryRepository.findById(directory.getId())
                .switchIfEmpty(noDirectoryMsg(directory.toString()))
                .flatMap(v -> {
                    if (reactiveDirectoryRepository.findById(newDirectoryId)
                            .blockOptional().isPresent()) {
                        return noDirectoryMsg(newDirectoryId.toString());
                    }
                    v.setParentId(newDirectoryId);
                    return reactiveDirectoryRepository.save(v);
                });
    }

    /**
     * 更新文件目录属主信息
     *
     * @param directory
     * @param owners
     * @return
     */
    public Mono<FsDirectory> updateFsOwners(FsDirectory directory, FsOwner[] owners) {
        return reactiveDirectoryRepository.findById(directory.getId())
                .switchIfEmpty(noDirectoryMsg(directory.toString()))
                .flatMap(v -> {
                    v.setOwners(owners);
                    return reactiveDirectoryRepository.save(v);
                });
    }

    /**
     * 删除文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directory
     * @return
     */
    public Mono<Void> deleteDirectory(FsDirectory directory) {
        return deleteDirectory(directory.getId());
    }

    /**
     * 删除文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directoryId
     * @return
     */
    public Mono<Void> deleteDirectory(ObjectId directoryId) {
        return reactiveDirectoryRepository.findById(directoryId)
                .switchIfEmpty(noDirectoryMsg(directoryId.toString()))
                .flatMap(v -> {
                    Flux<FsDirectory> subFlux = reactiveDirectoryRepository.findAllByParentId(directoryId);
                    if (subFlux == null || subFlux.count().block() == 0) {
                        return reactiveDirectoryRepository.delete(v);
                    } else {
                        return notEmptyDirMsg("[" + directoryId.toString() + "] " + v.getPath());
                    }
                });
    }

    /**
     * 根据上级目录ID信息获取子文件目录
     *
     * @param parentId
     * @return
     */
    public Flux<WebDirectory> findAllByParentIdInWebFormat(ObjectId parentId) {
        return switchToWebFormat(findAllByParentId(parentId));
    }

    public Flux<FsDirectory> findAllByParentId(ObjectId parentId) {
        return reactiveDirectoryRepository.findAllByParentId(parentId);
    }

    public Flux<WebDirectory> findAllInWebFormat() {
        return switchToWebFormat(findAll());
    }

    public Flux<FsDirectory> findAll() {
        return reactiveDirectoryRepository.findAll();
    }

    private Flux<WebDirectory> switchToWebFormat(Flux<FsDirectory> fsDirectoryFlux) {
        return fsDirectoryFlux.map(directory -> {
            WebDirectory webDirectory = new WebDirectory();
            BeanUtils.copyProperties(directory, webDirectory);
            Long dirCount = mongoTemplate.count(
                    Query.query(Criteria.where("parentId").is(directory.getId())),
                    FsDirectory.class);
            Long filesCount = mongoTemplate.count(
                    Query.query(Criteria.where("directoryId").is(directory.getId())),
                    FsDetail.class);
            webDirectory.setChildrenCount(dirCount + filesCount);
            return webDirectory;
        });
    }

    /**
     * 获取文件目录
     *
     * @param path     文件路径名
     * @param parentId 上级目录ID
     * @return
     */
    public Mono<FsDirectory> findByPathAndParentId(String path, ObjectId parentId) {
        return reactiveDirectoryRepository.findByPathAndParentId(path, parentId);
    }

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    public Mono<FsDirectory> findById(ObjectId id) {
        return reactiveDirectoryRepository.findById(id);
    }

}
