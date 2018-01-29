package cn.mxleader.quickdoc.common;

import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.security.entities.ActiveUser;

public class AuthenticationHandler {

    public static final Integer READ_PRIVILEGE = 1;
    public static final Integer WRITE_PRIVILEGE = 2;
    public static final Integer DELETE_PRIVILEGE = 4;

    public static final FsOwner SYSTEM_ADMIN_GROUP_OWNER = new FsOwner("administrators",
            FsOwner.Type.TYPE_GROUP, 7);

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param owners
     * @return
     */
    public static Boolean checkAuthentication(Boolean publicVisible, FsOwner[] owners,
                                              ActiveUser activeUser, Integer privilege) {
        // 管理员默认可访问所有目录和文件
        if (activeUser.isAdmin()) {
            return true;
        }
        // 公开访问权限仅设置读权限
        if (publicVisible && privilege == READ_PRIVILEGE) {
            return true;
        }
        if (owners != null && owners.length > 0) {
            for (FsOwner owner : owners) {
                if ((owner.getPrivilege() & privilege) > 0) {
                    switch (owner.getType()) {
                        case TYPE_GROUP:
                            for (String group : activeUser.getGroups()) {
                                if (owner.getName().equalsIgnoreCase(group)) {
                                    return true;
                                }
                            }
                        case TYPE_PRIVATE:
                            if (owner.getName().equalsIgnoreCase(activeUser.getUsername())) {
                                return true;
                            } else {
                                break;
                            }
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
