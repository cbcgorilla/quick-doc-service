package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.QuickDocUser

data class WebUser(val id: String, val username: String, val title: String,
                   val avatarId: String, val email: String? = null,
                   var groups: Array<String>) {
    constructor(user: QuickDocUser) : this(user.id.toString(), user.username,
            user.title, user.avatarId.toString(), user.email, user.groups)
}