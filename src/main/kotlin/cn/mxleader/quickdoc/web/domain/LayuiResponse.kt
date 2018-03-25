package cn.mxleader.quickdoc.web.domain

data class LayuiResponse<T>(val code: Int, val msg: String, var data: T)