package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.entities.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveMongoRepository<UserEntity, String> {
    Mono<UserEntity> findByUsername(String username);
}
