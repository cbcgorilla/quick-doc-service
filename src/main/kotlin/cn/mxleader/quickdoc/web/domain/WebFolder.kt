package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.AccessAuthorization
import org.bson.types.ObjectId

data class WebFolder(var id: ObjectId,
                     var path: String,
                     var parentId: ObjectId,
                     var childrenCount: Long,
                     var subFolder: WebFolder? = null,
                     var openAccess: Boolean? = false,
                     var authorizations: Array<AccessAuthorization>? = null,
                     var editAuthorization: Boolean? = false,
                     var deleteAuthorization: Boolean? = false) {
    constructor() : this(ObjectId.get(), "", ObjectId.get(),0L)
}