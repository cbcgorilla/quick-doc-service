package cn.mxleader.quickdoc.entities

data class RestResponse<T>(val action: String,
                           val code: CODE,
                           val result: T? = null) {
    enum class CODE {
        SUCCESS, FAIL, ERROR
    }
}
