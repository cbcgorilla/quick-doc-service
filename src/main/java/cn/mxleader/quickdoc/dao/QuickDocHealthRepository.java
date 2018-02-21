package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.QuickDocHealth;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuickDocHealthRepository extends MongoRepository<QuickDocHealth, ObjectId> {
    QuickDocHealth findByServiceAddress(String serviceAddress);
}