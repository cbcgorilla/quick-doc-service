package com.neofinance.quickdoc.repository;

import com.neofinance.quickdoc.common.entities.FsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileEntityRepository extends MongoRepository<FsEntity, Long> {

    FsEntity findByFilenameAndDirectoryId(String filename, Long directoryId);

    FsEntity findAllByDirectoryId(Long directoryId);
}