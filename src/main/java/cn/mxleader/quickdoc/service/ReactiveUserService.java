package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.QuickDocUser;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserService {

    Mono<QuickDocUser> saveUser(QuickDocUser quickDocUser);
    Flux<QuickDocUser> findAllUsers();
    Mono<QuickDocUser> findUser(String username);
    Mono<QuickDocUser> validateUser(String username, String password);
    Mono<Void> deleteUserById(ObjectId userId);
    Mono<Void> deleteUserByUsername(String username);

}
