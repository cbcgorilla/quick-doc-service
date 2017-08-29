package com.neofinance.quickdoc.repository;

import com.neofinance.quickdoc.common.entities.FsEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ReactiveFileEntityRepository extends ReactiveMongoRepository<FsEntity, Long> {

}
