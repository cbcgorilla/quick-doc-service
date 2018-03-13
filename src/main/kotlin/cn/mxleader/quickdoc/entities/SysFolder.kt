package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class SysFolder(@Id var id: ObjectId,
                     var name: String,
                     var parentId: ObjectId,
                     var authorizations: Array<AccessAuthorization>? = null) {

}