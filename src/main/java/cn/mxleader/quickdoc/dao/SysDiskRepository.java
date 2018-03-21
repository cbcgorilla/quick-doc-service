package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.SysDisk;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SysDiskRepository extends MongoRepository<SysDisk, ObjectId> {

    List<SysDisk> findAllByAuthorizationsContains(AccessAuthorization authorization);
}
