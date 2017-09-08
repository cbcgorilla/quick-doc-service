package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.entities.FsDirectory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DirectoryRepository extends MongoRepository<FsDirectory, Long> {

    FsDirectory findByPathAndParentId(String path, Long parentId);

    List<FsDirectory> findAllByParentId(Long parentId);

    Long countFsDirectoriesByParentIdIs(Long parentId);
}
