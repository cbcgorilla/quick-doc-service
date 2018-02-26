package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.ReactiveFolderRepository;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.QuickDocFolder;
import cn.mxleader.quickdoc.service.ReactiveFolderService;
import cn.mxleader.quickdoc.web.domain.WebFolder;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static cn.mxleader.quickdoc.common.utils.MessageUtil.*;

@Service
public class ReactiveFolderServiceImpl implements ReactiveFolderService {

    private final ReactiveFolderRepository reactiveFolderRepository;
    private final MongoTemplate mongoTemplate;

    ReactiveFolderServiceImpl(ReactiveFolderRepository reactiveFolderRepository,
                              MongoTemplate mongoTemplate) {
        this.reactiveFolderRepository = reactiveFolderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 新增文件目录, 默认上级目录ID为0；
     *
     * @param path
     * @param parentId
     * @param authorizations
     * @return
     */
    public Mono<QuickDocFolder> save(String path, ObjectId parentId,
                                     Boolean openAccess, AccessAuthorization[] authorizations) {
        return reactiveFolderRepository.findByPathAndParentId(path, parentId)
                .defaultIfEmpty(new QuickDocFolder(ObjectId.get(), path, parentId, openAccess, authorizations))
                .flatMap(folder -> {
                    folder.setOpenAccess(openAccess);
                    folder.setAuthorizations(authorizations);
                    return reactiveFolderRepository.save(folder);
                });
    }

    public Mono<QuickDocFolder> save(ObjectId folderId, String path, Boolean openAccess, AccessAuthorization[] authorizations) {
        return reactiveFolderRepository.findById(folderId)
                .flatMap(folder -> {
                    folder.setPath(path);
                    folder.setOpenAccess(openAccess);
                    folder.setAuthorizations(authorizations);
                    return reactiveFolderRepository.save(folder);
                });
    }

    /**
     * 新增文件目录;
     *
     * @param quickDocFolder
     * @return
     */
    public Mono<QuickDocFolder> save(QuickDocFolder quickDocFolder) {
        return save(quickDocFolder.getPath(),
                quickDocFolder.getParentId(),
                quickDocFolder.getOpenAccess(),
                quickDocFolder.getAuthorizations());
    }

    /**
     * 重命名文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folder
     * @param newPath
     * @return
     */
    public Mono<QuickDocFolder> rename(QuickDocFolder folder, String newPath) {
        return reactiveFolderRepository.findById(folder.getId())
                .switchIfEmpty(noDirectoryMsg(folder.toString()))
                .flatMap(v -> {
                    /*mongoTemplate.exists(
                            Query.query(
                                    Criteria.where("path").is(newPath).and("parentId").in(directory.getParentId())),
                            QuickDocFolder.class);*/
                    if (reactiveFolderRepository.findByPathAndParentId(newPath,
                            folder.getParentId()).blockOptional().isPresent()) {
                        return dirConflictMsg(newPath);
                    }
                    v.setPath(newPath);
                    return reactiveFolderRepository.save(v);
                });
    }

    /**
     * 迁移文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folderId    待迁移文件夹ID
     * @param newParentId 新上级目录ID
     * @return
     */
    public Mono<QuickDocFolder> move(ObjectId folderId, ObjectId newParentId) {
        return reactiveFolderRepository.findById(folderId)
                .switchIfEmpty(noDirectoryMsg(folderId))
                .flatMap(v -> {
                    if (!reactiveFolderRepository.findById(newParentId)
                            .blockOptional().isPresent()) {
                        return noDirectoryMsg(newParentId.toString());
                    }
                    v.setParentId(newParentId);
                    return reactiveFolderRepository.save(v);
                });
    }

    /**
     * 删除文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folderId
     * @return
     */
    public Mono<Void> delete(ObjectId folderId) {
        return reactiveFolderRepository.findById(folderId)
                .switchIfEmpty(noDirectoryMsg(folderId.toString()))
                .flatMap(v -> {
                    if (mongoTemplate.count(
                            Query.query(Criteria.where("parentId").is(folderId)),
                            QuickDocFolder.class) == 0) {
                        return reactiveFolderRepository.delete(v);
                    } else {
                        return notEmptyDirMsg("[" + folderId.toString() + "] " + v.getPath());
                    }
                });
    }

    /**
     * 根据上级目录ID信息获取子文件目录
     *
     * @param parentId
     * @return
     */
    public Flux<WebFolder> findAllByParentIdInWebFormat(ObjectId parentId) {
        return switchToWebFormat(findAllByParentId(parentId));
    }

    public Flux<QuickDocFolder> findAllByParentId(ObjectId parentId) {
        return reactiveFolderRepository.findAllByParentId(parentId);
    }

    public Flux<WebFolder> findAllInWebFormat() {
        return switchToWebFormat(findAll());
    }

    public Flux<QuickDocFolder> findAll() {
        return reactiveFolderRepository.findAll();
    }

    private Flux<WebFolder> switchToWebFormat(Flux<QuickDocFolder> folderFlux) {
        return folderFlux.map(folder -> {
            WebFolder webFolder = new WebFolder();
            BeanUtils.copyProperties(folder, webFolder);
            Long dirCount = mongoTemplate.count(
                    Query.query(Criteria.where("parentId").is(folder.getId())),
                    QuickDocFolder.class);
            Long filesCount = mongoTemplate.count(Query.query(GridFsCriteria
                            .whereMetaData("folderId").is(folder.getId())),
                    "fs.files");
            webFolder.setChildrenCount(dirCount + filesCount);
            return webFolder;
        });
    }

    /**
     * 获取文件目录
     *
     * @param path     文件路径名
     * @param parentId 上级目录ID
     * @return
     */
    public Mono<QuickDocFolder> findByPathAndParentId(String path, ObjectId parentId) {
        return reactiveFolderRepository.findByPathAndParentId(path, parentId);
    }

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    public Mono<QuickDocFolder> findById(ObjectId id) {
        return reactiveFolderRepository.findById(id);
    }

}
