package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class QuickDocFolder(@Id var id: ObjectId,
                          var path: String,
                          var parentId: ObjectId,
                          var openAccess:Boolean=false,
                          var authorizations: Array<AccessAuthorization>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuickDocFolder

        if (id != other.id) return false
        if (path != other.path) return false
        if (parentId != other.parentId) return false
        if (openAccess != other.openAccess) return false
        if (!Arrays.equals(authorizations, other.authorizations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + parentId.hashCode()
        result = 31 * result + openAccess.hashCode()
        result = 31 * result + (authorizations?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}