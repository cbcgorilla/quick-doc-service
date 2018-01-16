package cn.mxleader.quickdoc.persistent.dao;

import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.entities.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveMongoRepository<UserEntity, String> {
    Mono<UserEntity> findByUsername(String username);
}
