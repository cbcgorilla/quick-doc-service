package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.web.domain.WebUser;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    SysUser saveUser(SysUser sysUser);
    List<SysUser> list();
    Page<SysUser> list(Pageable pageable);
    SysUser get(String username);
    SysUser update(WebUser user);
    Boolean validateUser(String username, String password);
    void delete(ObjectId userId);
    SysUser addGroup(ObjectId id, String group);
    SysUser removeGroup(ObjectId id, String group);

}
