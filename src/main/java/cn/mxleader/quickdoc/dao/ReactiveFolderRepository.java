package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.SysFolder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFolderRepository extends ReactiveMongoRepository<SysFolder, ObjectId> {
    @Query("{ 'path': ?0, 'parentId': ?1}")
    Mono<SysFolder> findByPathAndParentId(String path, ObjectId parentId);

    Flux<SysFolder> findAllByParentId(ObjectId parentId);
    Flux<SysFolder> findAllByParentIdIsNull();
}
