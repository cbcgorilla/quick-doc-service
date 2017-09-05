package cn.techfan.quickdoc.service;

import cn.techfan.quickdoc.common.entities.WebUser;
import cn.techfan.quickdoc.repository.ReactiveUserRepository;
import cn.techfan.quickdoc.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static cn.techfan.quickdoc.common.utils.KeyUtil.SHA256Encrypt;

@Service
public class UserAuthenticationService {
    private final UserRepository userRepository;
    private final ReactiveUserRepository reactiveUserRepository;

    public UserAuthenticationService(UserRepository userRepository,
                                     ReactiveUserRepository reactiveUserRepository) {
        this.userRepository = userRepository;
        this.reactiveUserRepository = reactiveUserRepository;
    }

    public Mono<WebUser> saveUser(WebUser webUser, Boolean updateFlag) {
        return reactiveUserRepository.findByUsername(webUser.getUsername())
                .defaultIfEmpty(webUser)
                .flatMap(entity -> {
                    if (updateFlag) {
                        entity.setPassword(SHA256Encrypt(webUser.getPassword()));
                        entity.setAuthorities(webUser.getAuthorities());
                    }
                    return reactiveUserRepository.save(entity);
                });
    }

    public Mono<WebUser> findUser(String username) {
        return reactiveUserRepository.findByUsername(username);
    }

    public Mono<WebUser> validateUser(String username, String password) {
        String encyptPassword = SHA256Encrypt(password);
        return findUser(username)
                .defaultIfEmpty(new WebUser("", "", "", null))
                .map(user -> {
                    if (!user.getUsername().equalsIgnoreCase(username)) {
                        user.setAuthorities(null);
                        return user;
                    }
                    if (!user.getPassword().equalsIgnoreCase(encyptPassword)) {
                        user.setAuthorities(null);
                        return user;
                    }
                    return user;
                });
    }
}
