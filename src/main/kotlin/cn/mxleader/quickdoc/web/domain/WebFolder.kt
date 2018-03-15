package cn.mxleader.quickdoc.web.domain

import org.bson.types.ObjectId

data class WebFolder(var id: ObjectId,
                     var name: String,
                     var parentId: ObjectId,
                     var childrenCount: Long,
                     var subFolder: WebFolder? = null,
                     var editAuthorization: Boolean? = false,
                     var deleteAuthorization: Boolean? = false) {
    constructor() : this(ObjectId.get(), "", ObjectId.get(),0L)
}