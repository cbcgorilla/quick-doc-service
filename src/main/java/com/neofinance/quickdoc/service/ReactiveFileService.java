package com.neofinance.quickdoc.service;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.neofinance.quickdoc.common.entities.FsEntity;
import com.neofinance.quickdoc.common.utils.GridFsAssistant;
import com.neofinance.quickdoc.repository.FileEntityRepository;
import com.neofinance.quickdoc.repository.ReactiveFileEntityRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

import static com.neofinance.quickdoc.common.query.QueryKit.keyQuery;
import static com.neofinance.quickdoc.common.utils.ReactiveErrorMessage.fileNotExistMsg;

@Service
public class ReactiveFileService {

    private final GridFsAssistant gridFsAssistant;
    private final GridFsTemplate gridFsTemplate;
    private final FileEntityRepository fileEntityRepository;
    private final ReactiveFileEntityRepository reactiveFileEntityRepository;

    ReactiveFileService(GridFsAssistant gridFsAssistant,
                        GridFsTemplate gridFsTemplate,
                        FileEntityRepository fileEntityRepository,
                        ReactiveFileEntityRepository reactiveFileEntityRepository) {
        this.gridFsAssistant = gridFsAssistant;
        this.gridFsTemplate = gridFsTemplate;
        this.fileEntityRepository = fileEntityRepository;
        this.reactiveFileEntityRepository = reactiveFileEntityRepository;
    }

    public Mono<FsEntity> getStoredFile(String filename, Long directoryId) {
        return reactiveFileEntityRepository.findByFilenameAndDirectoryId(filename, directoryId);
    }

    public Flux<FsEntity> getStoredFiles(Long directoryId) {
        return reactiveFileEntityRepository.findAllByDirectoryId(directoryId);
    }

    /**
     * 存储文件， 如同名文件已存在则更新文件内容
     *
     * @param fileEntity
     * @return
     */
    public Mono<FsEntity> storeFile(FsEntity fileEntity, InputStream file) {
        return getStoredFile(fileEntity.getFilename(), fileEntity.getDirectoryId())
                .defaultIfEmpty(fileEntity)
                .flatMap(
                        entity -> {
                            // 删除库中同名历史文件
                            deleteFile(entity);
                            entity.setStoredId(
                                    gridFsTemplate.store(file,
                                            fileEntity.getFilename(),
                                            fileEntity.getContentType()
                                    )
                            );
                            return reactiveFileEntityRepository.save(entity);
                        }
                );
    }

    /**
     * 删除Mongo库内文件
     *
     * @param fileEntity 文件信息
     * @return
     */
    public Mono<Void> deleteFile(FsEntity fileEntity) {
        return getStoredFile(fileEntity.getFilename(), fileEntity.getDirectoryId())
                .switchIfEmpty(
                        fileNotExistMsg(new Long(fileEntity.getDirectoryId()).toString(),
                                fileEntity.getFilename())
                )
                .flatMap(entity -> {
                    gridFsTemplate.delete(keyQuery(entity.getStoredId()));
                    return reactiveFileEntityRepository.delete(entity);
                });
    }

    public GridFSDownloadStream getFileStream(ObjectId storedId) {
        return gridFsAssistant.getResource(storedId);
    }

}