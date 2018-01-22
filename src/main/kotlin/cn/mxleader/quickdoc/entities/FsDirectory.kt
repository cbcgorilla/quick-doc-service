package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class FsDirectory(@Id var id: ObjectId,
                       var path: String,
                       var parentId: ObjectId,
                       var publicVisible:Boolean=false,
                       var owners: Array<FsOwner>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FsDirectory

        if (id != other.id) return false
        if (path != other.path) return false
        if (parentId != other.parentId) return false
        if (publicVisible != other.publicVisible) return false
        if (!Arrays.equals(owners, other.owners)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + parentId.hashCode()
        result = 31 * result + publicVisible.hashCode()
        result = 31 * result + (owners?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}