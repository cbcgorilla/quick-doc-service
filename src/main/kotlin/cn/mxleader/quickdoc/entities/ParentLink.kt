package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId

data class ParentLink(val id: ObjectId, val target:AuthTarget)