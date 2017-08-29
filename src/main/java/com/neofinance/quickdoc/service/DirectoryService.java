package com.neofinance.quickdoc.service;

import com.neofinance.quickdoc.common.ConflictException;
import com.neofinance.quickdoc.common.entities.FsDirectory;
import com.neofinance.quickdoc.common.entities.FsOwner;
import com.neofinance.quickdoc.common.utils.KeyUtil;
import com.neofinance.quickdoc.repository.DirectoryRepository;
import com.neofinance.quickdoc.repository.ReactiveDirectoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DirectoryService {

    private static final String MSG_NO_DIRECTORY = "[Exception from DirectoryService] 找不到文件目录：";
    private static final String MSG_DIRECTORY_CONFLICT = "[Exception from DirectoryService] 与已有文件目录冲突：";
    private static final String MSG_NON_NULL_DIRECTORY = "[Exception from DirectoryService] 文件夹非空：";

    private final DirectoryRepository directoryRepository;
    private final ReactiveDirectoryRepository reactiveDirectoryRepository;

    DirectoryService(DirectoryRepository directoryRepository, ReactiveDirectoryRepository reactiveDirectoryRepository) {
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
        return reactiveDirectoryRepository.findByPathAndParent(path, parentId)
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
                .switchIfEmpty(Mono.error(
                        new NoSuchElementException(MSG_NO_DIRECTORY + directory)))
                .flatMap(v -> {
                    if (directoryRepository.findByPathAndParent(newPath, directory.getParent()) != null) {
                        return Mono.error(new ConflictException(MSG_DIRECTORY_CONFLICT + newPath));
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
                .switchIfEmpty(Mono.error(
                        new NoSuchElementException(MSG_NO_DIRECTORY + directory)))
                .flatMap(v -> {
                    if (directoryRepository.findById(newDirectoryId) != null) {
                        return Mono.error(new NoSuchElementException(MSG_NO_DIRECTORY + newDirectoryId));
                    }
                    v.setParent(newDirectoryId);
                    return reactiveDirectoryRepository.save(v);
                });
    }

    /**
     * 更新文件目录属主信息
     * @param directory
     * @param owners
     * @return
     */
    public Mono<FsDirectory> updateFsOwners(FsDirectory directory, FsOwner[] owners) {
        return reactiveDirectoryRepository.findById(directory.getId())
                .switchIfEmpty(Mono.error(
                        new NoSuchElementException(MSG_NO_DIRECTORY + directory)))
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
                .switchIfEmpty(Mono.error(
                        new NoSuchElementException(MSG_NO_DIRECTORY + directoryId)))
                .flatMap(v -> {
                    List<FsDirectory> subList = directoryRepository.findAllByParent(directoryId);
                    if (subList == null || subList.size() == 0) {
                        return reactiveDirectoryRepository.delete(v);
                    } else {
                        return Mono.error(new ConflictException(MSG_NON_NULL_DIRECTORY + directoryId));
                    }
                });
    }

    /**
     * 根据上级目录ID信息获取子文件目录
     *
     * @param parent
     * @return
     */
    public Flux<FsDirectory> findAllByParent(Long parent) {
        return reactiveDirectoryRepository.findAllByParent(parent);
    }

    /**
     * 获取文件目录
     *
     * @param path  文件路径名
     * @param parent 上级目录ID
     * @return
     */
    public Mono<FsDirectory> findByPathAndParent(String path, Long parent) {
        return reactiveDirectoryRepository.findByPathAndParent(path, parent);
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
        return reactiveDirectoryRepository.findAllByParent(0L);
    }

}
