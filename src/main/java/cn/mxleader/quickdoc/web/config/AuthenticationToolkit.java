package cn.mxleader.quickdoc.web.config;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.security.entities.ActiveUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthenticationToolkit {

    public static final Integer READ_PRIVILEGE = 1;
    public static final Integer WRITE_PRIVILEGE = 2;
    public static final Integer DELETE_PRIVILEGE = 4;

    public static final AccessAuthorization SYSTEM_ADMIN_GROUP_OWNER = new AccessAuthorization("administrators",
            AccessAuthorization.Type.TYPE_GROUP, 7);


    public static Boolean checkAuthentication(Boolean openAccess, AccessAuthorization[] authorizations,
                                              ActiveUser activeUser, Integer privilege) {
        return checkAuthentication(openAccess, Arrays.asList(authorizations), activeUser, privilege);
    }

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param openAccess     公开访问开关
     * @param authorizations 授权列表
     * @param activeUser     用户信息
     * @param privilege      待校验权限级别（READ_PRIVILEGE，WRITE_PRIVILEGE，DELETE_PRIVILEGE）
     * @return 鉴权通过返回True，否则返回False
     */
    public static Boolean checkAuthentication(Boolean openAccess, List<AccessAuthorization> authorizations,
                                              ActiveUser activeUser, Integer privilege) {
        // 管理员默认可访问所有目录和文件
        if (activeUser.isAdmin()) {
            return true;
        }
        // 公开访问权限仅设置读权限
        if (openAccess && privilege == READ_PRIVILEGE) {
            return true;
        }
        if (authorizations != null && authorizations.size() > 0) {
            for (AccessAuthorization authorization : authorizations) {
                if ((authorization.getPrivilege() & privilege) > 0) {
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

    public static Boolean getOpenAccessFromOwnerRequest(String[] ownersRequest) {
        if (ownersRequest != null && ownersRequest.length > 0) {
            for (String item : ownersRequest) {
                if (item.equalsIgnoreCase("PublicMode")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static AccessAuthorization[] translateOwnerRequest(ActiveUser activeUser, String[] ownersRequest) {
        AccessAuthorization owner = new AccessAuthorization(activeUser.getUsername(),
                AccessAuthorization.Type.TYPE_PRIVATE, 7);
        List<AccessAuthorization> accessAuthorizationList = new ArrayList<AccessAuthorization>();
        accessAuthorizationList.add(owner);
        if (ownersRequest != null && ownersRequest.length > 0) {
            for (String item : ownersRequest) {
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
    }
}
