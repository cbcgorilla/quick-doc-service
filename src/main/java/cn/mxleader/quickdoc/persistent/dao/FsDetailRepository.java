package cn.mxleader.quickdoc.persistent.dao;

import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FsDetailRepository extends MongoRepository<FsDetail, String> {

    FsDetail findByFilenameAndDirectoryId(String filename, Long directoryId);

    List<FsDetail> findAllByDirectoryId(Long directoryId);

    List<FsDetail> findAllByDirectoryIdAndCategoryId(Long directoryId, Long categoryId);

    Long countFsEntitiesByDirectoryIdIs(Long directoryId);
}
