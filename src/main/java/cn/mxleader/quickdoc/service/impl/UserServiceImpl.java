package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysUserRepository;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.common.utils.PasswordUtil;
import cn.mxleader.quickdoc.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {
    private final SysUserRepository userRepository;

    public UserServiceImpl(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SysUser saveUser(SysUser sysUser) {
        sysUser.setPassword(PasswordUtil.getEncryptedPwd(sysUser.getPassword()));
        return userRepository.save(sysUser);
    }

    public List<SysUser> findAllUsers() {
        return userRepository.findAll();
    }

    public SysUser findUser(String username) {
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
        SysUser sysUser = findUser(username);
        if (sysUser == null) {
            return false;
        } else {
            return PasswordUtil.validPassword(password, sysUser.getPassword());
        }
    }

    public void deleteUserById(ObjectId userId) {
        userRepository.deleteById(userId);
    }

    public void deleteUserByUsername(String username) {
        deleteUserById(findUser(username).getId());
    }

}
