package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class SysFolder(@Id var id: ObjectId,
                     var name: String,
                     var parents: Array<ParentLink>,
                     var authorizations: Array<AccessAuthorization>? = null)