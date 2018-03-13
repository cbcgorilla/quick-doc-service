package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.SysProfile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SysProfileRepository extends MongoRepository<SysProfile, ObjectId> {
    SysProfile findByServiceAddress(String serviceAddress);
}