package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SysFolderRepository extends MongoRepository<SysFolder, ObjectId> {

    List<SysFolder> findAllByParentsContaining(ParentLink parent);

    Optional<SysFolder> findByParentsContainsAndName(ParentLink parent, String name);

    @Query(value = "{ 'parents.diskId' : ?0 }",
            fields = "{ 'name' : 1, 'parents' : {$elemMatch: {'diskId': ?0}  }, 'authorizations': 1 }")
    List<SysFolder> findAllByParentsDiskId(ObjectId diskId);
}
