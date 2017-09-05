package cn.techfan.quickdoc.service;

import cn.techfan.quickdoc.common.entities.FsDirectory;
import cn.techfan.quickdoc.common.entities.FsOwner;
import cn.techfan.quickdoc.common.utils.KeyUtil;
import cn.techfan.quickdoc.repository.DirectoryRepository;
import cn.techfan.quickdoc.repository.ReactiveDirectoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static cn.techfan.quickdoc.common.utils.MessageUtil.*;

@Service
public class ReactiveDirectoryService {

    private final DirectoryRepository directoryRepository;
    private final ReactiveDirectoryRepository reactiveDirectoryRepository;

    ReactiveDirectoryService(DirectoryRepository directoryRepository,
                             ReactiveDirectoryRepository reactiveDirectoryRepository) {
        this.directoryRepository = directoryRepository;
        this.reactiveDirectoryRepository = reactiveDirectoryRepository;
    }

    /**
     * 新增文件目录, 默认上级目录ID为0；
     *
     * @param path
     * @param parent
     * @param owners
     * @return
     */
    public Mono<FsDirectory> addDirectory(String path, FsDirectory parent, FsOwner[] owners) {
        long parentId = parent != null ? parent.getId() : 0L;
        return reactiveDirectoryRepository.findByPathAndParentId(path, parentId)
                .defaultIfEmpty(new FsDirectory(KeyUtil.longID(), path, parentId, owners))
                .flatMap(reactiveDirectoryRepository::save);
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
    public Mono<FsDirectory> removeDirectory(FsDirectory directory, FsDirectory newDirectory) {
        return removeDirectory(directory, newDirectory.getId());
    }

    /**
     * 迁移文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param directory      待迁移文件夹
     * @param newDirectoryId 新上级目录ID
     * @return
     */
    public Mono<FsDirectory> removeDirectory(FsDirectory directory, Long newDirectoryId) {
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
    public Flux<FsDirectory> findAllByParentId(Long parentId) {
        return reactiveDirectoryRepository.findAllByParentId(parentId);
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

    /**
     * 获取所有根目录
     *
     * @return
     */
    public Flux<FsDirectory> allRootDirectories() {
        return reactiveDirectoryRepository.findAllByParentId(0L);
    }

}
