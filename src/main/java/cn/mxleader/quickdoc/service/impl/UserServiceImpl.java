package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.PasswordUtil;
import cn.mxleader.quickdoc.dao.SysUserRepository;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import cn.mxleader.quickdoc.web.domain.WebUser;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


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
    public List<SysUser> listDeptUsers(String department){
        return userRepository.findAllByDepartment(department);
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
            BeanUtils.copyProperties(webUser, user, getEmptyPropertyNames(webUser));
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser changePassword(ObjectId id, String newPassword) {
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

    @Override
    public SysUser updateGroups(ObjectId id, Set<String> groups) {
        Optional<SysUser> optionalSysUser = userRepository.findById(id);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.setGroups(groups);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser addAuth(ObjectId userId, SysUser.Authority authority) {
        Optional<SysUser> optionalSysUser = userRepository.findById(userId);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.addAuthority(authority);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser removeAuth(ObjectId userId, SysUser.Authority authority) {
        Optional<SysUser> optionalSysUser = userRepository.findById(userId);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.removeAuthority(authority);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser addManagePath(ObjectId id, String path) {
        Optional<SysUser> optionalSysUser = userRepository.findById(id);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.addManagePath(path);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public SysUser removeManagePath(ObjectId id, String path) {
        Optional<SysUser> optionalSysUser = userRepository.findById(id);
        if (optionalSysUser.isPresent()) {
            SysUser user = optionalSysUser.get();
            user.removeManagePath(path);
            return userRepository.save(user);
        }
        return null;
    }

    /**
     * 取非空属性名称列表
     *
     * @param source
     * @return
     */
    private static String[] getEmptyPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null || srcValue.toString().length() == 0) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

}
