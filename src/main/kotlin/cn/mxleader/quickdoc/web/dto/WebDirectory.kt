package cn.mxleader.quickdoc.web.dto

import cn.mxleader.quickdoc.entities.FsOwner
import org.bson.types.ObjectId

data class WebDirectory(var id: ObjectId,
                        var path: String,
                        var parentId: ObjectId,
                        var owners: Array<FsOwner>? = null,
                        var childrenCount: Long? = null) {
    constructor() : this(ObjectId.get(), "", ObjectId.get(), null, null)
}