package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthType;
import cn.mxleader.quickdoc.entities.SysDisk;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

public interface SysDiskRepository extends MongoRepository<SysDisk, ObjectId> {

    @Query("{ 'authorizations.name' : ?0, 'authorizations.type' : ?1, 'authorizations.actions' : {$all: ?2 }}")
    List<SysDisk> findAllByAuthorizations(String name, AuthType type, Set<AuthAction> actions);
}
