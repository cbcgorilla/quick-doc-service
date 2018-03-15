package cn.mxleader.quickdoc.dao;

import cn.mxleader.quickdoc.entities.SysUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SysUserRepository extends MongoRepository<SysUser, ObjectId> {
    SysUser findByUsername(String username);
    List<SysUser> findByGroupsContaining(String group);
}
