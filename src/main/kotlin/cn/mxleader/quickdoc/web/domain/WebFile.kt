package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.AccessAuthorization
import org.bson.types.ObjectId
import java.util.*

data class WebFile(val id: ObjectId,
                   val filename: String,
                   val length: Long,
                   val uploadDate: Date,
                   val type: String,
                   val folderId: ObjectId,
                   val openAccess: Boolean = false,
                   val authorizations: Array<AccessAuthorization>? = null,
                   var labels: Array<String>? = null,
                   var linkPrefix: String? = null,
                   var iconClass: String? = null,
                   var editAuthorization:Boolean ? = false,
                   var deleteAuthorization: Boolean ? = false) {
    constructor(id: ObjectId,
                filename: String,
                length: Long,
                uploadDate: Date,
                type: String,
                folderId: ObjectId,
                openAccess: Boolean = false,
                authorizations: Array<AccessAuthorization>? = null,
                labels: Array<String>? = null) : this(id, filename, length, uploadDate, type, folderId,
            openAccess, authorizations, labels, null, null)
}