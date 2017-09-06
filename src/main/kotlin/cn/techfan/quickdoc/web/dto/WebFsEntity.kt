package cn.techfan.quickdoc.web.dto

import cn.techfan.quickdoc.common.entities.FsOwner
import org.bson.types.ObjectId
import java.util.*


data class WebFsEntity(var id: String,
                       var filename: String,
                       var contentLength: Long,
                       var contentType: String,
                       var uploadDate: Date,
                       var categoryId: Long,
                       var directoryId: Long,
                       var storedId: ObjectId? = null,
                       var owners: Array<FsOwner>? = null,
                       var category: String? = null,
                       var directory: String? = null)
