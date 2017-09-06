package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.common.entities.FsEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFileEntityRepository extends ReactiveMongoRepository<FsEntity, String> {

    Mono<FsEntity> findByFilenameAndDirectoryId(String filename, Long directoryId);

    Flux<FsEntity> findAllByDirectoryId(Long directoryId);
}
