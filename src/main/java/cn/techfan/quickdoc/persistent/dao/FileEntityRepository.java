package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.common.entities.FsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileEntityRepository extends MongoRepository<FsEntity, String> {

    FsEntity findByFilenameAndDirectoryId(String filename, Long directoryId);

    List<FsEntity> findAllByDirectoryId(Long directoryId);

    List<FsEntity> findAllByDirectoryIdAndCategoryId(Long directoryId, Long categoryId);
}
