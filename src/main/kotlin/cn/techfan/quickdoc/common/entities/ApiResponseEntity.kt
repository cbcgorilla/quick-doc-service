package cn.techfan.quickdoc.common.entities

data class ApiResponseEntity<T>(var action: String,
                                var code: Code,
                                var result: T) {
    enum class Code {
        SUCCESS, FAIL, ERROR
    }
}