package com.neofinance.quickdoc.common.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class FsEntity(@Id val id: String,
                    val filename: String,
                    val contentLength: Long,
                    val contentType: String,
                    val uploadDate: Date,
                    val category: FsCategory,
                    val directoryId: Long,
                    var owners: Array<FsOwner>? = null)
