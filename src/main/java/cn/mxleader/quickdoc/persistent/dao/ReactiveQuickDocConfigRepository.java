package cn.mxleader.quickdoc.persistent.dao;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveQuickDocConfigRepository extends ReactiveMongoRepository<QuickDocConfig, Long> {

    Mono<QuickDocConfig> findByServiceAddress(String serviceAddress);
}