package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.SysUser

data class WebUser(val id: String,
                   var username: String,
                   var displayName: String,
                   var title: String,
                   var avatarId: String,
                   var ldap: Boolean,
                   var department: String,
                   var authorities: Set<SysUser.Authority>,
                   var groups: Set<String>,
                   var email: String? = null) {
    constructor(user: SysUser) : this(user.id.toString(), user.username, user.displayName,
            user.title, user.avatarId.toString(), user.ldap, user.department, user.authorities, user.groups, user.email)
}