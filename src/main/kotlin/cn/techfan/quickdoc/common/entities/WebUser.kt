package cn.techfan.quickdoc.common.entities

import org.springframework.data.annotation.Id

data class WebUser(@Id val id: String, var username: String, var password: String, var authorities: Array<String>? = null)