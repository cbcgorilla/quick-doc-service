package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserService {

    Mono<UserEntity> saveUser(UserEntity userEntity);
    Flux<UserEntity> findAllUsers();
    Mono<UserEntity> findUser(String username);
    Mono<UserEntity> validateUser(String username, String password);
    Mono<Void> deleteUserById(String userId);
    Mono<Void> deleteUserByUsername(String username);

}
