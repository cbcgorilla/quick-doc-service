package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.QuickDocUser

data class WebUser(val id: String, val username: String, var groups: Array<String>) {
    constructor(user: QuickDocUser) : this(user.id.toString(), user.username, user.groups)
}