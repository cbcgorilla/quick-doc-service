package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.ReactiveDirectoryRepository;
import cn.mxleader.quickdoc.entities.FsDescription;
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
                                           Boolean publicVisible, FsOwner[] owners) {
        return reactiveDirectoryRepository.findByPathAndParentId(path, parentId)
                .defaultIfEmpty(new FsDirectory(ObjectId.get(), path, parentId, publicVisible, owners))
                .flatMap(fsDirectory -> {
                    fsDirectory.setPublicVisible(publicVisible);
                    fsDirectory.setOwners(owners);
                    return reactiveDirectoryRepository.save(fsDirectory);
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
                    /*mongoTemplate.exists(
                            Query.query(
                                    Criteria.where("path").is(newPath).and("parentId").in(directory.getParentId())),
                            FsDirectory.class);*/
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
     * @param directoryId 待迁移文件夹ID
     * @param newParentId 新上级目录ID
     * @return
     */
    public Mono<FsDirectory> moveDirectory(ObjectId directoryId, ObjectId newParentId) {
        return reactiveDirectoryRepository.findById(directoryId)
                .switchIfEmpty(noDirectoryMsg(directoryId))
                .flatMap(v -> {
                    if (!reactiveDirectoryRepository.findById(newParentId)
                            .blockOptional().isPresent()) {
                        return noDirectoryMsg(newParentId.toString());
                    }
                    v.setParentId(newParentId);
                    return reactiveDirectoryRepository.save(v);
                });
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
                    if (mongoTemplate.count(
                            Query.query(Criteria.where("parentId").is(directoryId)),
                            FsDirectory.class) == 0 &&
                            mongoTemplate.count(
                                    Query.query(Criteria.where("directoryId").is(directoryId)),
                                    FsDescription.class) == 0) {
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
                    FsDescription.class);
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
