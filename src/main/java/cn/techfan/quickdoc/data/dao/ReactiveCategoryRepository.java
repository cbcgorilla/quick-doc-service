package cn.techfan.quickdoc.data.dao;

import cn.techfan.quickdoc.common.entities.FsCategory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveCategoryRepository extends ReactiveMongoRepository<FsCategory, Long> {

    Mono<FsCategory> findByType(String type);

}

