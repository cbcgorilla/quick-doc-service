package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class FsDetail(@Id var id: ObjectId,
                    var filename: String,
                    var contentLength: Long,
                    var contentType: String,
                    var uploadDate: Date,
                    var categoryId: ObjectId,
                    var directoryId: ObjectId,
                    var storedId: ObjectId,
                    var publicVisible:Boolean = false,
                    var owners: Array<FsOwner>? = null,
                    var category: String? = null,
                    var directory: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FsDetail

        if (id != other.id) return false
        if (filename != other.filename) return false
        if (contentLength != other.contentLength) return false
        if (contentType != other.contentType) return false
        if (uploadDate != other.uploadDate) return false
        if (categoryId != other.categoryId) return false
        if (directoryId != other.directoryId) return false
        if (storedId != other.storedId) return false
        if (publicVisible != other.publicVisible) return false
        if (!Arrays.equals(owners, other.owners)) return false
        if (category != other.category) return false
        if (directory != other.directory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + contentLength.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + categoryId.hashCode()
        result = 31 * result + directoryId.hashCode()
        result = 31 * result + storedId.hashCode()
        result = 31 * result + publicVisible.hashCode()
        result = 31 * result + (owners?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + (directory?.hashCode() ?: 0)
        return result
    }
}