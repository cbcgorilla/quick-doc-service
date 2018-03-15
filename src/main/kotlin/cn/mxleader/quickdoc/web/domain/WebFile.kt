package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.AccessAuthorization
import java.util.*

data class WebFile(val id: String,
                   val filename: String,
                   val length: Long,
                   val uploadDate: Date,
                   val type: String,
                   val folderId: String,
                   var labels: Array<String>? = null,
                   var linkPrefix: String? = null,
                   var iconClass: String? = null,
                   var editAuthorization: Boolean? = false,
                   var deleteAuthorization: Boolean? = false) {
    constructor(id: String,
                filename: String,
                length: Long,
                uploadDate: Date,
                type: String,
                folderId: String,
                labels: Array<String>? = null) : this(id, filename, length,
            uploadDate, type, folderId, labels, null, null)
}