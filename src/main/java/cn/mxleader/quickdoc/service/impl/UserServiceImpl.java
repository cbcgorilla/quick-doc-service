package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.PasswordUtil;
import cn.mxleader.quickdoc.dao.SysUserRepository;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import cn.mxleader.quickdoc.web.domain.WebUser;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<SysUser> list() {
        return userRepository.findAll();
    }

    @Override
    public Page<SysUser> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public SysUser get(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public SysUser update(WebUser webUser) {
        Optional<SysUser> userOptional = userRepository.findById(new ObjectId(webUser.getId()));
        if (userOptional.isPresent()) {
            SysUser user = userOptional.get();
            user.setUsername(webUser.getUsername());
            user.setTitle(webUser.getTitle());
            user.setEmail(webUser.getEmail());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser changePassword(ObjectId id, String newPassword){
        Optional<SysUser> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            SysUser user = userOptional.get();
            user.setPassword(PasswordUtil.getEncryptedPwd(newPassword));
            return userRepository.save(user);
        }
        return null;
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
        SysUser sysUser = get(username);
        if (sysUser == null) {
            return false;
        } else {
            return PasswordUtil.validPassword(password, sysUser.getPassword());
        }
    }

    @Override
    public void delete(ObjectId userId) {
        userRepository.deleteById(userId);
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
