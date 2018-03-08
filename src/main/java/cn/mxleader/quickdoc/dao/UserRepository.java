package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.QuickDocUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<QuickDocUser, ObjectId> {
    QuickDocUser findByUsername(String username);
}
