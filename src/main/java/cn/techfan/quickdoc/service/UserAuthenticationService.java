package cn.techfan.quickdoc.service;

import cn.techfan.quickdoc.entities.UserEntity;
import cn.techfan.quickdoc.persistent.dao.ReactiveUserRepository;
import cn.techfan.quickdoc.persistent.dao.UserRepository;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static cn.techfan.quickdoc.common.utils.MessageUtil.invalidPasswordMsg;
import static cn.techfan.quickdoc.common.utils.MessageUtil.userNotFoundMsg;

@Service
public class UserAuthenticationService {
    private final UserRepository userRepository;
    private final ReactiveUserRepository reactiveUserRepository;
    private static final String salt = "Ak@47";

    public UserAuthenticationService(UserRepository userRepository,
                                     ReactiveUserRepository reactiveUserRepository) {
        this.userRepository = userRepository;
        this.reactiveUserRepository = reactiveUserRepository;
    }

    public Mono<UserEntity> saveUser(UserEntity userEntity, Boolean updateFlag) {
        return reactiveUserRepository.findByUsername(userEntity.getUsername())
                .defaultIfEmpty(userEntity)
                .flatMap(entity -> {
                    if (updateFlag) {
                        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
                        entity.setPassword(encoder.encodePassword(userEntity.getPassword(), salt));
                        entity.setAuthorities(userEntity.getAuthorities());
                    }
                    return reactiveUserRepository.save(entity);
                });
    }

    public Mono<UserEntity> findUser(String username) {
        return reactiveUserRepository.findByUsername(username);
    }

    public Mono<UserEntity> validateUser(String username, String password) {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        return findUser(username)
                .defaultIfEmpty(new UserEntity("", "", "", null))
                .map(user -> {
                    if (!user.getUsername().equalsIgnoreCase(username)) {
                        userNotFoundMsg(username);
                    }
                    if (!encoder.isPasswordValid(user.getPassword(), password, salt)) {
                        invalidPasswordMsg();
                    }
                    return user;
                });
    }
}
