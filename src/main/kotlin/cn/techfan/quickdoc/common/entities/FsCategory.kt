package cn.techfan.quickdoc.common.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class FsCategory(@Id val id:Long, var type: String)
