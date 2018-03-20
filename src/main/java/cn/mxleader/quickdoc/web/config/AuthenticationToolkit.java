package cn.mxleader.quickdoc.web.config;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.SysUser;

import java.util.List;

public class AuthenticationToolkit {

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param authorizations 授权列表
     * @param activeUser     用户信息
     * @param action         待校验权限级别（READ，WRITE，DELETE）
     * @return 鉴权通过返回True，否则返回False
     */
    public static Boolean checkAuthentication(List<AccessAuthorization> authorizations,
                                              SysUser activeUser, AccessAuthorization.Action action) {
        // 管理员默认可访问所有目录和文件
        if (activeUser.isAdmin()) {
            return true;
        }
        if (authorizations != null && authorizations.size() > 0) {
            for (AccessAuthorization authorization : authorizations) {
                if (authorization.getAction().equals(action)) {
                    switch (authorization.getType()) {
                        case TYPE_GROUP:
                            for (String group : activeUser.getGroups()) {
                                if (authorization.getName().equalsIgnoreCase(group)) {
                                    return true;
                                }
                            }
                        case TYPE_PRIVATE:
                            if (authorization.getName().equalsIgnoreCase(activeUser.getUsername())) {
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
/*
    public static AccessAuthorization[] translateShareSetting(SysUser activeUser,
                                                              String[] shareSetting) {
        AccessAuthorization owner = new AccessAuthorization(activeUser.getUsername(),
                AccessAuthorization.Type.TYPE_PRIVATE, 7);
        List<AccessAuthorization> accessAuthorizationList = new ArrayList<AccessAuthorization>();
        accessAuthorizationList.add(owner);
        if (shareSetting != null && shareSetting.length > 0) {
            for (String item : shareSetting) {
                if (item.equalsIgnoreCase("GroupMode")) {
                    for (String group : activeUser.getGroups()) {
                        accessAuthorizationList.add(new AccessAuthorization(group,
                                AccessAuthorization.Type.TYPE_GROUP, 3));
                    }
                }
            }
        }
        AccessAuthorization[] accessAuthorizationDesc = new AccessAuthorization[accessAuthorizationList.size()];
        return accessAuthorizationList.toArray(accessAuthorizationDesc);
    }*/
/*
    public static AccessAuthorization[] translateShareSetting(SysUser activeUser,
                                                              String[] ownersRequest,
                                                              String[] shareGroups) {
        AccessAuthorization owner = new AccessAuthorization(activeUser.getUsername(),
                AccessAuthorization.Type.TYPE_PRIVATE, 7);
        List<AccessAuthorization> accessAuthorizationList = new ArrayList<AccessAuthorization>();
        accessAuthorizationList.add(owner);
        if (ownersRequest != null && ownersRequest.length > 0) {
            for (String item : ownersRequest) {
                if (item.equalsIgnoreCase("GroupMode")) {
                    for (String group : shareGroups) {
                        accessAuthorizationList.add(new AccessAuthorization(group,
                                AccessAuthorization.Type.TYPE_GROUP, 3));
                    }
                }
            }
        }
        AccessAuthorization[] accessAuthorizationDesc = new AccessAuthorization[accessAuthorizationList.size()];
        return accessAuthorizationList.toArray(accessAuthorizationDesc);
    }*/
}
