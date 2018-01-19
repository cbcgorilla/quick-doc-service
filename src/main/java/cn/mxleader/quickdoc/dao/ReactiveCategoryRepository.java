package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsCategory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveCategoryRepository extends ReactiveMongoRepository<FsCategory, ObjectId> {

    Mono<FsCategory> findByType(String type);

}

