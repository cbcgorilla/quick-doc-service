package com.neofinance.quickdoc.common.entities

import org.springframework.data.annotation.Id

data class WebUser(@Id val id: String, val username: String, var password: String, var authorities: Array<String>? = null)