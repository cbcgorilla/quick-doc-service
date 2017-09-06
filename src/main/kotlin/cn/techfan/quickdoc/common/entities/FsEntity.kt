package cn.techfan.quickdoc.common.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class FsEntity(@Id val id: String,
                    var filename: String,
                    var contentLength: Long,
                    var contentType: String,
                    var uploadDate: Date,
                    var categoryId: Long,
                    var directoryId: Long,
                    var storedId: ObjectId?=null,
                    var owners: Array<FsOwner>? = null,
                    var category:String?=null,
                    var directory:String?=null)
