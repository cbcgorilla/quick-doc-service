package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.QuickDocFolder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFolderRepository extends ReactiveMongoRepository<QuickDocFolder, ObjectId> {
    @Query("{ 'path': ?0, 'parentId': ?1}")
    Mono<QuickDocFolder> findByPathAndParentId(String path, ObjectId parentId);

    Flux<QuickDocFolder> findAllByParentId(ObjectId parentId);
}
