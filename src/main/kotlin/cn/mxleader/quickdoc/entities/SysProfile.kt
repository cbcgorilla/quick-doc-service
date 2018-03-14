package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class SysProfile(@Id val id: ObjectId,
                      var serviceAddress: String,
                      var iconMap: Map<String, ObjectId>,
                      var initialized: Boolean,
                      var setupTime: Date,
                      var startup: Date? = null)