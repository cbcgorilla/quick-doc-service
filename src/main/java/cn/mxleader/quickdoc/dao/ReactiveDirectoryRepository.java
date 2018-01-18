package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsDirectory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDirectoryRepository extends ReactiveMongoRepository<FsDirectory, Long> {

    Mono<FsDirectory> findByPathAndParentId(String path, Long parentId);

    Flux<FsDirectory> findAllByParentId(Long parentId);
    Mono<Long> countFsDirectoriesByParentIdIs(Long parentId);
}
