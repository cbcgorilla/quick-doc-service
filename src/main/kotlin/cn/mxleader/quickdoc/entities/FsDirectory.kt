package cn.mxleader.quickdoc.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class FsDirectory(@Id var id: Long,
                  var path: String,
                  var parentId: Long,
                  var owners: Array<FsOwner>? = null)