package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId

data class FileMetadata(var _contentType: String,
                        var folderId: ObjectId,
                        var openAccess: Boolean = false,
                        var authorizations: Array<AccessAuthorization>? = null,
                        var labels: Array<String>? = null) {
    constructor() : this("", ObjectId.get(), false, null, null)
}