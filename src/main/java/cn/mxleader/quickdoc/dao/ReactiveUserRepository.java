package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveMongoRepository<UserEntity, ObjectId> {
    Mono<UserEntity> findByUsername(String username);
}
