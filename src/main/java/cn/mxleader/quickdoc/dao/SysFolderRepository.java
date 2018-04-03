package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SysFolderRepository extends MongoRepository<SysFolder, ObjectId> {

    List<SysFolder> findAllByParent(ParentLink parent);

    Optional<SysFolder> findByParentAndName(ParentLink parent, String name);
/*
    @Query(value = "{ 'parent.diskId' : ?0 }",
            fields = "{ 'name' : 1, 'parent' : {$elemMatch: {'diskId': ?0}  }, 'authorization': 1 }")*/
    List<SysFolder> findAllByParentDiskId(ObjectId diskId);
}
