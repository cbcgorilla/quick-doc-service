package cn.mxleader.quickdoc.common.utils;

import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.security.session.ActiveUser;

public class AuthenticationUtil {

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param owners
     * @return
     */
    public static Boolean checkAuthentication(FsOwner[] owners, ActiveUser activeUser, Integer privilege) {
        // 管理员默认可访问所有目录和文件
        if (activeUser.isAdmin()) {
            return true;
        }
        if (owners != null && owners.length > 0) {
            for (FsOwner owner : owners) {
                int read = owner.getPrivilege() & privilege;
                if (read > 0) {
                    switch (owner.getType()) {
                        case TYPE_PUBLIC:
                            return true;
                        case TYPE_GROUP:
                            if (owner.getName().equalsIgnoreCase(activeUser.getGroup())) {
                                return true;
                            } else {
                                break;
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