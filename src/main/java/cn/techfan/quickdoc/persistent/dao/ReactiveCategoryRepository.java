package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.entities.FsCategory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveCategoryRepository extends ReactiveMongoRepository<FsCategory, Long> {

    Mono<FsCategory> findByType(String type);

}

