package cn.mxleader.quickdoc.web.domain

data class LayuiData<T>(val code: Int, val msg: String, var count:Int, var data: T)