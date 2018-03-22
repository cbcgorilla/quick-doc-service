package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.SysUser

data class WebUser(val id: String,
var username: String,
var title: String,
var avatarId: String,
var groups: List<String>,
var email: String? = null) {
    constructor(user:SysUser):this(user.id.toString(),user.username,user.title,user.avatarId.toString(),user.groups,user.email)
}