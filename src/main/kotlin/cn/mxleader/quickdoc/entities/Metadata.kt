package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import java.util.*

data class Metadata(var _contentType: String,
                    var folderId: ObjectId,
                    var authorizations: Array<AccessAuthorization>? = null,
                    var labels: Array<String>? = null) {
    constructor() : this("", ObjectId.get(), null, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        if (_contentType != other._contentType) return false
        if (folderId != other.folderId) return false
        if (!Arrays.equals(authorizations, other.authorizations)) return false
        if (!Arrays.equals(labels, other.labels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _contentType.hashCode()
        result = 31 * result + folderId.hashCode()
        result = 31 * result + (authorizations?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (labels?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }

}