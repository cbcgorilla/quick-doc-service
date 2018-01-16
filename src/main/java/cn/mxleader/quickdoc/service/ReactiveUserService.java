package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.common.utils.MessageUtil;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.persistent.dao.ReactiveUserRepository;
import cn.mxleader.quickdoc.common.utils.MD5Util;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static cn.mxleader.quickdoc.common.utils.KeyUtil.stringUUID;

@Service
public class ReactiveUserService {
    private final ReactiveUserRepository reactiveUserRepository;

    public ReactiveUserService(ReactiveUserRepository reactiveUserRepository) {
        this.reactiveUserRepository = reactiveUserRepository;
    }

    public Mono<UserEntity> saveUser(UserEntity userEntity) {
        userEntity.setId(stringUUID());
        return reactiveUserRepository.findByUsername(userEntity.getUsername())
                .defaultIfEmpty(userEntity)
                .flatMap(entity -> {
                    entity.setPassword(MD5Util.getEncryptedPwd(userEntity.getPassword()));
                    entity.setAuthorities(userEntity.getAuthorities());
                    return reactiveUserRepository.save(entity);
                });
    }

    public Flux<UserEntity> findAllUsers() {
        return reactiveUserRepository.findAll();
    }

    public Mono<UserEntity> findUser(String username) {
        return reactiveUserRepository.findByUsername(username);
    }

    /**
     * 检查用户名密码是否有效
     *
     * @param username 输入用户名
     * @param password 输入密码（明文）
     * @return
     */
    public Mono<UserEntity> validateUser(String username, String password) {
        return findUser(username)
                .switchIfEmpty(MessageUtil.userNotFoundMsg(username))
                .flatMap(user -> {
                    if (!MD5Util.validPassword(password, user.getPassword())) {
                        return MessageUtil.invalidPasswordMsg(password);
                    }
                    return Mono.just(user);
                });
    }

    public Mono<Void> deleteUser(String username) {
        return findUser(username).flatMap(reactiveUserRepository::delete);
    }

}
