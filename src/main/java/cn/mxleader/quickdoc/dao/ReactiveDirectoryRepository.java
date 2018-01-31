package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsDirectory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDirectoryRepository extends ReactiveMongoRepository<FsDirectory, ObjectId> {
    @Query("{ 'path': ?0, 'parentId': ?1}")
    Mono<FsDirectory> findByPathAndParentId(String path, ObjectId parentId);

    Flux<FsDirectory> findAllByParentId(ObjectId parentId);
}
