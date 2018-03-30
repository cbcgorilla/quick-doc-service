package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SysDiskRepository extends MongoRepository<SysDisk, ObjectId> {

    List<SysDisk> findAllByAuthorization(Authorization authorization);
}
