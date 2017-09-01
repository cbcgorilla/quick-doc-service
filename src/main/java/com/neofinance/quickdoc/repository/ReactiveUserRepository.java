package com.neofinance.quickdoc.repository;

import com.neofinance.quickdoc.common.entities.WebUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveMongoRepository<WebUser, String> {
    Mono<WebUser> findByUsername(String username);
}
