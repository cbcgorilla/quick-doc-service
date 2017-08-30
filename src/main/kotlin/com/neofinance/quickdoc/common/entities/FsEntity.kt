package com.neofinance.quickdoc.common.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class FsEntity(@Id val id: String,
                    var filename: String,
                    val contentLength: Long,
                    val contentType: String,
                    val uploadDate: Date,
                    var categoryId: Long,
                    var directoryId: Long,
                    var storedId: ObjectId?=null,
                    var owners: Array<FsOwner>? = null)
