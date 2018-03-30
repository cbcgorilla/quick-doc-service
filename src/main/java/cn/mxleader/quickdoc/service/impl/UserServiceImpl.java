package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.dao.SysUserRepository;
import cn.mxleader.quickdoc.entities.SysFolder;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.common.utils.PasswordUtil;
import cn.mxleader.quickdoc.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {
    private final SysUserRepository userRepository;

    public UserServiceImpl(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SysUser saveUser(SysUser sysUser) {
        sysUser.setPassword(PasswordUtil.getEncryptedPwd(sysUser.getPassword()));
        return userRepository.save(sysUser);
    }

    @Override
    public List<SysUser> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
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
    @Override
    public Boolean validateUser(String username, String password) {
        SysUser sysUser = findUser(username);
        if (sysUser == null) {
            return false;
        } else {
            return PasswordUtil.validPassword(password, sysUser.getPassword());
        }
    }

    @Override
    public void deleteUserById(ObjectId userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void deleteUserByUsername(String username) {
        deleteUserById(findUser(username).getId());
    }

    @Override
    public SysUser addGroup(ObjectId id, String group) {
        Optional<SysUser> optionalSysUser = userRepository.findById(id);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.addGroup(group);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser removeGroup(ObjectId id, String group) {
        Optional<SysUser> optionalSysUser = userRepository.findById(id);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.removeGroup(group);
            return userRepository.save(user);
        }
        return null;
    }

}
