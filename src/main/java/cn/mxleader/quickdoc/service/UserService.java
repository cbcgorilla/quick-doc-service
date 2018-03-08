package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.QuickDocUser;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {

    QuickDocUser saveUser(QuickDocUser quickDocUser);
    List<QuickDocUser> findAllUsers();
    QuickDocUser findUser(String username);
    Boolean validateUser(String username, String password);
    void deleteUserById(ObjectId userId);
    void deleteUserByUsername(String username);

}
