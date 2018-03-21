package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class SysDisk(@Id var id: ObjectId, var name: String, var authorizations: List<AccessAuthorization>? = null)