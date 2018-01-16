package cn.mxleader.quickdoc.web.dto

import cn.mxleader.quickdoc.entities.FsOwner
import org.bson.types.ObjectId
import java.util.*

class WebFsEntity(
        val id: String,
        val filename: String,
        val contentLength: Long,
        val contentType: String,
        val uploadDate: Date,
        val categoryId: Long,
        val directoryId: Long,
        val storedId: ObjectId,
        val owners: Array<FsOwner>? = null,
        val category: String,
        val directory: String)