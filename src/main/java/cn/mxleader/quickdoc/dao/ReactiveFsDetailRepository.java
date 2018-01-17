package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsDetail;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFsDetailRepository extends ReactiveMongoRepository<FsDetail, String> {

    Mono<FsDetail> findByFilenameAndDirectoryId(String filename, Long directoryId);

    Flux<FsDetail> findAllByDirectoryId(Long directoryId);

    Mono<Long> countFsEntitiesByDirectoryIdIs(Long directoryId);
}
