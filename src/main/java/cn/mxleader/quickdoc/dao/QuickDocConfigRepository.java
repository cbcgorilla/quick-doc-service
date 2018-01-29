package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.QuickDocConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuickDocConfigRepository extends MongoRepository<QuickDocConfig, ObjectId> {

    QuickDocConfig findByServiceAddress(String serviceAddress);
}