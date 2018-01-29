package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.MessageUtil;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.dao.ReactiveUserRepository;
import cn.mxleader.quickdoc.common.utils.PasswordUtil;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class ReactiveUserServiceImpl implements ReactiveUserService {
    private final ReactiveUserRepository reactiveUserRepository;

    public ReactiveUserServiceImpl(ReactiveUserRepository reactiveUserRepository) {
        this.reactiveUserRepository = reactiveUserRepository;
    }

    public Mono<UserEntity> saveUser(UserEntity userEntity) {
        userEntity.setId(ObjectId.get());
        return reactiveUserRepository.findByUsername(userEntity.getUsername())
                .defaultIfEmpty(userEntity)
                .flatMap(entity -> {
                    entity.setPassword(PasswordUtil.getEncryptedPwd(userEntity.getPassword()));
                    entity.setRoles(userEntity.getRoles());
                    entity.setPrivileges(userEntity.getPrivileges());
                    entity.setGroups(userEntity.getGroups());
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
                    if (!PasswordUtil.validPassword(password, user.getPassword())) {
                        return MessageUtil.invalidPasswordMsg(password);
                    }
                    return Mono.just(user);
                });
    }

    public Mono<Void> deleteUserById(ObjectId userId) {
        return reactiveUserRepository.findById(userId)
                .flatMap(reactiveUserRepository::delete);
    }

    public Mono<Void> deleteUserByUsername(String username) {
        return findUser(username).flatMap(reactiveUserRepository::delete);
    }

}
