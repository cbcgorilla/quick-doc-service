package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsDescription;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFsDetailRepository extends ReactiveMongoRepository<FsDescription, ObjectId> {

    Mono<FsDescription> findByFilenameAndDirectoryId(String filename, ObjectId directoryId);

    Flux<FsDescription> findAllByDirectoryId(ObjectId directoryId);

    Flux<FsDescription> findAllByDirectoryIdAndCategoryId(ObjectId directoryId, ObjectId categoryId);

}
