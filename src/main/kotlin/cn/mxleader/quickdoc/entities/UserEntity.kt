package cn.mxleader.quickdoc.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class UserEntity(@Id var id:String,
                      var username:String,
                      var password:String,
                      var authorities:Array<String>?=null,
                      var group:String?=null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (!Arrays.equals(authorities, other.authorities)) return false
        if (group != other.group) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + (authorities?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (group?.hashCode() ?: 0)
        return result
    }
}