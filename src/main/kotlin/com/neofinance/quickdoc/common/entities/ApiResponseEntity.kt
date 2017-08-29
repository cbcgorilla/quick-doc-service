package com.neofinance.quickdoc.common.entities

data class ApiResponseEntity<T>(val action: String,
                                val code: Code,
                                val result: T) {
    enum class Code {
        SUCCESS, FAIL
    }
}