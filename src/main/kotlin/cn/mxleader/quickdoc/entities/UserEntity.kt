package cn.mxleader.quickdoc.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class UserEntity(@Id var id:String, var username:String, var password:String, var authorities:Array<String>?=null)