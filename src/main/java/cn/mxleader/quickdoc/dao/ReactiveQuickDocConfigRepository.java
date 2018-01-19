package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveQuickDocConfigRepository extends ReactiveMongoRepository<QuickDocConfig, ObjectId> {

    Mono<QuickDocConfig> findByServiceAddress(String serviceAddress);
}