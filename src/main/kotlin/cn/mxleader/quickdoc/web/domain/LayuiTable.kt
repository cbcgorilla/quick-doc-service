package cn.mxleader.quickdoc.web.domain

data class LayuiTable<T>(val code: Int, val msg:String, val count:Int, var data:List<T>)