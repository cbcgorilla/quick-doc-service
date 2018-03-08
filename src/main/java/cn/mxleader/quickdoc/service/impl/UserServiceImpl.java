package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.UserRepository;
import cn.mxleader.quickdoc.entities.QuickDocUser;
import cn.mxleader.quickdoc.security.utils.PasswordUtil;
import cn.mxleader.quickdoc.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public QuickDocUser saveUser(QuickDocUser quickDocUser) {
        quickDocUser.setPassword(PasswordUtil.getEncryptedPwd(quickDocUser.getPassword()));
        return userRepository.save(quickDocUser);
    }

    public List<QuickDocUser> findAllUsers() {
        return userRepository.findAll();
    }

    public QuickDocUser findUser(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 检查用户名密码是否有效
     *
     * @param username 输入用户名
     * @param password 输入密码（明文）
     * @return
     */
    public Boolean validateUser(String username, String password) {
        QuickDocUser quickDocUser = findUser(username);
        if (quickDocUser == null) {
            return false;
        } else {
            return PasswordUtil.validPassword(password, quickDocUser.getPassword());
        }
    }

    public void deleteUserById(ObjectId userId) {
        userRepository.deleteById(userId);
    }

    public void deleteUserByUsername(String username) {
        deleteUserById(findUser(username).getId());
    }

}
