package cn.mxleader.quickdoc.entities

import cn.mxleader.quickdoc.web.session.ActiveUserStore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import javax.servlet.http.HttpSessionBindingEvent
import javax.servlet.http.HttpSessionBindingListener

@Document
class SysUser(@Id var id: ObjectId,
              var username: String,
              var displayName:String,
              var title: String,
              var password: String,
              var avatarId: ObjectId,
              var authorities: Set<Authority>,
              var groups: Set<String>,
              var email: String? = null) : HttpSessionBindingListener {

    enum class Authority {
        ADMIN, USER
    }

    fun isAdmin(): Boolean {
        for (it in authorities) {
            if (it == SysUser.Authority.ADMIN) {
                return true
            }
        }
        return false
    }

    override fun valueBound(event: HttpSessionBindingEvent) {
        val application = event.session.servletContext
        // 第一次使用前，需要初始化
        if (application.getAttribute("ActiveUserStore") == null) {
            application.setAttribute("ActiveUserStore", ActiveUserStore())
        }
        // 把用户名放入在线列表
        var activeUserStore: ActiveUserStore = application.getAttribute("ActiveUserStore") as ActiveUserStore
        activeUserStore.addUser(this.username)
    }

    override fun valueUnbound(event: HttpSessionBindingEvent) {
        val application = event.session.servletContext
        // 从在线列表中删除用户名
        val activeUserStore = application.getAttribute("ActiveUserStore") as ActiveUserStore
        activeUserStore.removeUser(this.username)
    }

    fun addAuthority(authority: Authority) {
        this.authorities += authority
    }

    fun removeAuthority(authority: Authority) {
        this.authorities -= authority
    }

    fun addGroup(group: String) {
        this.groups += group
    }

    fun removeGroup(group: String) {
        this.groups -= group
    }

}