package cn.mxleader.quickdoc.entities

sealed class RestResponse
data class SuccessResponse<out T>(val content: T) : RestResponse()
data class ErrorResponse(val code: Int, val message: String) : RestResponse()