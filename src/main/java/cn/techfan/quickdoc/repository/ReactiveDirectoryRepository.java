package cn.techfan.quickdoc.repository;

import cn.techfan.quickdoc.common.entities.FsDirectory;
import cn.techfan.quickdoc.common.entities.FsCategory;
import cn.techfan.quickdoc.common.entities.FsDirectory;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDirectoryRepository extends ReactiveMongoRepository<FsDirectory, Long> {

    Mono<FsDirectory> findByPathAndParentId(String path, Long parentId);

    Flux<FsDirectory> findAllByParentId(Long parentId);
}
