package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.AccessAuthorization
import org.bson.types.ObjectId

data class WebFolder(var id: ObjectId,
                     var path: String,
                     var parentId: ObjectId,
                     var openAccess:Boolean=false,
                     var authorizations: Array<AccessAuthorization>? = null,
                     var childrenCount: Long) {
    constructor() : this(ObjectId.get(), "", ObjectId.get(),
            false,null, 0L)
}