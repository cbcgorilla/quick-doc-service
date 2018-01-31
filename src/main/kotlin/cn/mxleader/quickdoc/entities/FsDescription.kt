package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class FsDescription(@Id var id: ObjectId,
                         var filename: String,
                         var size: Long,
                         var type: String,
                         var uploadTime: Date,
                         var categoryId: ObjectId,
                         var directoryId: ObjectId,
                         var storedId: ObjectId,
                         var openVisible:Boolean = false,
                         var owners: Array<FsOwner>? = null,
                         var labels:Array<String>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FsDescription

        if (id != other.id) return false
        if (filename != other.filename) return false
        if (size != other.size) return false
        if (type != other.type) return false
        if (uploadTime != other.uploadTime) return false
        if (categoryId != other.categoryId) return false
        if (directoryId != other.directoryId) return false
        if (storedId != other.storedId) return false
        if (openVisible != other.openVisible) return false
        if (!Arrays.equals(owners, other.owners)) return false
        if (!Arrays.equals(labels, other.labels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + uploadTime.hashCode()
        result = 31 * result + categoryId.hashCode()
        result = 31 * result + directoryId.hashCode()
        result = 31 * result + storedId.hashCode()
        result = 31 * result + openVisible.hashCode()
        result = 31 * result + (owners?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (labels?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}