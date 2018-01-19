package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.UserEntity;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserService {

    Mono<UserEntity> saveUser(UserEntity userEntity);
    Flux<UserEntity> findAllUsers();
    Mono<UserEntity> findUser(String username);
    Mono<UserEntity> validateUser(String username, String password);
    Mono<Void> deleteUserById(ObjectId userId);
    Mono<Void> deleteUserByUsername(String username);

}
