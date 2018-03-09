package cn.mxleader.quickdoc.entities

import cn.mxleader.quickdoc.web.session.ActiveUserStore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import javax.servlet.http.HttpSessionBindingEvent
import javax.servlet.http.HttpSessionBindingListener

@Document
class QuickDocUser(@Id var id: ObjectId,
                   var username: String,
                   var title: String,
                   var password: String,
                   var avatarId: ObjectId,
                   var authorities: Array<Authorities>,
                   var groups: Array<String>,
                   var email: String? = null) : HttpSessionBindingListener {

    enum class Authorities {
        ADMIN, USER
    }

    fun isAdmin():Boolean{
        for (it in authorities) {
            if (it == QuickDocUser.Authorities.ADMIN) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuickDocUser

        if (id != other.id) return false
        if (username != other.username) return false
        if (title != other.title) return false
        if (password != other.password) return false
        if (avatarId != other.avatarId) return false
        if (!Arrays.equals(authorities, other.authorities)) return false
        if (!Arrays.equals(groups, other.groups)) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + avatarId.hashCode()
        result = 31 * result + Arrays.hashCode(authorities)
        result = 31 * result + Arrays.hashCode(groups)
        result = 31 * result + (email?.hashCode() ?: 0)
        return result
    }

}