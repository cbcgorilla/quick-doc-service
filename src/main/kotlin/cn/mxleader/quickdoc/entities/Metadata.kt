package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId

data class Metadata(var _contentType: String,
                    var folders: List<ObjectId>? = null,
                    var authorizations: Array<AccessAuthorization>? = null,
                    var labels: Array<String>? = null) {
    constructor() : this("application/octet-stream", null, null, null)
}