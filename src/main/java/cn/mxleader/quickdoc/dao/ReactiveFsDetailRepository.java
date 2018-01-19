package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsDetail;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFsDetailRepository extends ReactiveMongoRepository<FsDetail, ObjectId> {

    Mono<FsDetail> findByFilenameAndDirectoryId(String filename, ObjectId directoryId);

    Flux<FsDetail> findAllByDirectoryId(ObjectId directoryId);

    Flux<FsDetail> findAllByDirectoryIdAndCategoryId(ObjectId directoryId, ObjectId categoryId);

    Mono<Long> countFsDetailsByDirectoryIdIs(ObjectId directoryId);
}
