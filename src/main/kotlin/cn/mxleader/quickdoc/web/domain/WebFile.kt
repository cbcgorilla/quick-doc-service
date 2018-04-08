package cn.mxleader.quickdoc.web.domain

import java.util.*

data class WebFile(val id: String,
                   val filename: String,
                   val length: Long,
                   val uploadDate: Date,
                   val type: String,
                   var iconClass: String? = null,
                   var editAuthorization: Boolean? = false,
                   var deleteAuthorization: Boolean? = false) {
    constructor(id: String,
                filename: String,
                length: Long,
                uploadDate: Date,
                type: String) : this(id, filename, length,
            uploadDate, type, null)
}