package cn.mxleader.quickdoc.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class QuickDocConfig(@Id val id: Long,
                          var serviceAddress: String,
                          var initialized: Boolean,
                          var setupTime: Date,
                          var startup: Date? = null)