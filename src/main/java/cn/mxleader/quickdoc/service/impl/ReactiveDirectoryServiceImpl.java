package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.DirectoryRepository;
import cn.mxleader.quickdoc.dao.FsDetailRepository;
import cn.mxleader.quickdoc.dao.ReactiveDirectoryRepository;
import cn.mxleader.quickdoc.entities.FsDirectory;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.web.dto.WebDirectory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static cn.mxleader.quickdoc.common.CommonCode.SYSTEM_ADMIN_GROUP_OWNER;
import static cn.mxleader.quickdoc.common.utils.KeyUtil.longID;
import static cn.mxleader.quickdoc.common.utils.MessageUtil.*;

@Service
public class ReactiveDirectoryServiceImpl implements ReactiveDirectoryService {

    private final DirectoryRepository directoryRepository;
    private final FsDetailRepository fsDetailRepository;
    private final ReactiveDirectoryRepository reactiveDirectoryRepository;

    ReactiveDirectoryServiceImpl(DirectoryRepository directoryRepository,
                                 FsDetailRepository fsDetailRepository,
                                 ReactiveDirectoryRepository reactiveDirectoryRepository) {
        this.directoryRepository = directoryRepository;
        this.fsDetailRepository = fsDetailRepository;
        this.reactiveDirectoryRepository = reactiveDirectoryRepository;
    }

    /**
     * 新增文件目录, 默认上级目录ID为0；
     *
     * @param path
     * @param parentId
     * @param owners
     * @return
     */
    public Mono<FsDirectory> saveDirectory(String path, Long parentId, FsOwner[] owners) {
        return reactiveDirectoryRepository.findByPathAndParentId(path, parentId)
                .defaultIfEmpty(new FsDirectory(longID(), path, parentId, owners))
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
                    if (directoryRepository.findByPathAndParentId(newPath,
                            directory.getParentId()) != null) {
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
    public Mono<FsDirectory> moveDirectory(FsDirectory directory, Long newDirectoryId) {
        return reactiveDirectoryRepository.findById(directory.getId())
                .switchIfEmpty(noDirectoryMsg(directory.toString()))
                .flatMap(v -> {
                    if (directoryRepository.findById(newDirectoryId) != null) {
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
    public Mono<Void> deleteDirectory(Long directoryId) {
        return reactiveDirectoryRepository.findById(directoryId)
                .switchIfEmpty(noDirectoryMsg(directoryId.toString()))
                .flatMap(v -> {
                    List<FsDirectory> subList = directoryRepository.findAllByParentId(directoryId);
                    if (subList == null || subList.size() == 0) {
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
    public Flux<WebDirectory> findAllByParentId(Long parentId) {
        return reactiveDirectoryRepository.findAllByParentId(parentId)
                .map(directory -> {
                    WebDirectory webDirectory = new WebDirectory();
                    BeanUtils.copyProperties(directory, webDirectory);
                    Long dirCount = directoryRepository.countFsDirectoriesByParentIdIs(directory.getId());
                    Long filesCount = fsDetailRepository.countFsEntitiesByDirectoryIdIs(directory.getId());
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
    public Mono<FsDirectory> findByPathAndParentId(String path, Long parentId) {
        return reactiveDirectoryRepository.findByPathAndParentId(path, parentId);
    }

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    public Mono<FsDirectory> findById(Long id) {
        return reactiveDirectoryRepository.findById(id);
    }

}
