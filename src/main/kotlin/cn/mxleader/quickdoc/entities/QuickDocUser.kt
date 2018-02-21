package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class QuickDocUser(@Id var id: ObjectId,
                        var username:String,
                        var password:String,
                        var authorities:Array<Authorities>,
                        var groups:Array<String>) {

    enum class Authorities {
        ADMIN, USER
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuickDocUser

        if (id != other.id) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (!Arrays.equals(authorities, other.authorities)) return false
        if (!Arrays.equals(groups, other.groups)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + Arrays.hashCode(authorities)
        result = 31 * result + Arrays.hashCode(groups)
        return result
    }

}