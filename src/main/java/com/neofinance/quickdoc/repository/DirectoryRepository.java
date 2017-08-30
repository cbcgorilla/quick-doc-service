package com.neofinance.quickdoc.repository;

import com.neofinance.quickdoc.common.entities.FsDirectory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DirectoryRepository extends MongoRepository<FsDirectory, Long> {

    FsDirectory findByPathAndParentId(String path, Long parentId);

    List<FsDirectory> findAllByParentId(Long parentId);
}
