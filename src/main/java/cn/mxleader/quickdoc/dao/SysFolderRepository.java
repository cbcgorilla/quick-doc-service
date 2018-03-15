package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.ParentLink;
import cn.mxleader.quickdoc.entities.SysFolder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SysFolderRepository extends MongoRepository<SysFolder, ObjectId> {
    List<SysFolder> findAllByParentsContains(ParentLink parentLink);
}
