package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsDirectory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDirectoryRepository extends ReactiveMongoRepository<FsDirectory, ObjectId> {

    Mono<FsDirectory> findByPathAndParentId(String path, ObjectId parentId);

    Flux<FsDirectory> findAllByParentId(ObjectId parentId);
    Mono<Long> countFsDirectoriesByParentIdIs(ObjectId parentId);
}
