package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysFolderRepository;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.service.FolderService;
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

import java.util.List;
import java.util.Optional;

import static cn.mxleader.quickdoc.common.utils.MessageUtil.*;

@Service
public class FolderServiceImpl implements FolderService {

    private final SysFolderRepository sysFolderRepository;
    private final MongoTemplate mongoTemplate;

    FolderServiceImpl(SysFolderRepository sysFolderRepository,
                      MongoTemplate mongoTemplate) {
        this.sysFolderRepository = sysFolderRepository;
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
    public Mono<SysFolder> save(String path, ObjectId parentId,
                                AccessAuthorization[] authorizations) {
        return reactiveFolderRepository.findByPathAndParentId(path, parentId)
                .defaultIfEmpty(new SysFolder(ObjectId.get(), path, parentId, authorizations))
                .flatMap(folder -> {
                    folder.setAuthorizations(authorizations);
                    return reactiveFolderRepository.save(folder);
                });
    }

    public Mono<SysFolder> save(ObjectId folderId, String path,
                                AccessAuthorization[] authorizations) {
        return reactiveFolderRepository.findById(folderId)
                .flatMap(folder -> {
                    folder.setName(path);
                    folder.setAuthorizations(authorizations);
                    return reactiveFolderRepository.save(folder);
                });
    }

    /**
     * 新增文件目录;
     *
     * @param sysFolder
     * @return
     */
    public Mono<SysFolder> save(SysFolder sysFolder) {
        return save(sysFolder.getName(),
                sysFolder.getParentId(),
                sysFolder.getAuthorizations());
    }

    /**
     * 重命名文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folder
     * @param newPath
     * @return
     */
    public Mono<SysFolder> rename(SysFolder folder, String newPath) {
        return reactiveFolderRepository.findById(folder.getId())
                .switchIfEmpty(noDirectoryMsg(folder.toString()))
                .flatMap(v -> {
                    if (reactiveFolderRepository.findByPathAndParentId(newPath,
                            folder.getParentId()).blockOptional().isPresent()) {
                        return dirConflictMsg(newPath);
                    }
                    v.setName(newPath);
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
    public Mono<SysFolder> move(ObjectId folderId, ObjectId newParentId) {
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
                            SysFolder.class) == 0) {
                        return reactiveFolderRepository.delete(v);
                    } else {
                        return notEmptyDirMsg("[" + folderId.toString() + "] " + v.getName());
                    }
                });
    }

    private Flux<WebFolder> switchToWebFormat(Flux<SysFolder> folderFlux) {
        return folderFlux.map(folder -> {
            WebFolder webFolder = new WebFolder();
            BeanUtils.copyProperties(folder, webFolder);
            Long dirCount = mongoTemplate.count(
                    Query.query(Criteria.where("parentId").is(folder.getId())),
                    SysFolder.class);
            Long filesCount = mongoTemplate.count(Query.query(GridFsCriteria
                            .whereMetaData("folderId").is(folder.getId())),
                    "fs.files");
            webFolder.setChildrenCount(dirCount + filesCount);
            return webFolder;
        });
    }

    @Override
    public List<SysFolder> findAllByParent(ParentLink parent) {
        return sysFolderRepository.findAllByParentsContains(parent);
    }

    @Override
    public List<WebFolder> findAllByParentInWebFormat(ParentLink parent) {
        return switchToWebFormat(Flux.just(findAllByParent(parent)));
    }

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    @Override
    public Optional<SysFolder> findById(ObjectId id) {
        return sysFolderRepository.findById(id);
    }

    @Override
    public SysFolder save(String name, ParentLink[] parents, AccessAuthorization[] authorizations) {
        return null;
    }

    @Override
    public SysFolder rename(ObjectId id, String newName) {
        return null;
    }

    @Override
    public SysFolder move(ObjectId id, ParentLink oldParent, ParentLink newParent) {
        return null;
    }

}
