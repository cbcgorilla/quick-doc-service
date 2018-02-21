package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.QuickDocUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveMongoRepository<QuickDocUser, ObjectId> {
    Mono<QuickDocUser> findByUsername(String username);
}
