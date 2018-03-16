package cn.mxleader.quickdoc.web.domain

import org.bson.types.ObjectId

data class WebFolder(var id: ObjectId,
                     var name: String) {
    constructor() : this(ObjectId.get(), "")
}