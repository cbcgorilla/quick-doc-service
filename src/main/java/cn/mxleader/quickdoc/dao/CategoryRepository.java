package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.FsCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<FsCategory, Long> {

    FsCategory findByType(String type);

}
