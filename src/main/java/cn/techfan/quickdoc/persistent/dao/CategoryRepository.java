package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.entities.FsCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<FsCategory, Long> {

    FsCategory findByType(String type);

}
