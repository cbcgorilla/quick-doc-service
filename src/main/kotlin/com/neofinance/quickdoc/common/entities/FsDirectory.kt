package com.neofinance.quickdoc.common.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class FsDirectory(@Id val id: Long,
                       var path: String,
                       var parentId: Long,
                       var owners: Array<FsOwner>? = null)
