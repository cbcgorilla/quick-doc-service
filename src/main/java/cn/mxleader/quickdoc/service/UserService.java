package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.SysUser;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {

    SysUser saveUser(SysUser sysUser);
    List<SysUser> findAllUsers();
    SysUser findUser(String username);
    Boolean validateUser(String username, String password);
    void deleteUserById(ObjectId userId);
    void deleteUserByUsername(String username);
    SysUser addGroup(ObjectId id, String group);
    SysUser removeGroup(ObjectId id, String group);

}
