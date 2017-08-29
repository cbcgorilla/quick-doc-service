package com.neofinance.quickdoc.common.entities

data class FsOwner(val username: String, val type: Type){
    enum class Type {
        TYPE_PUBLIC, TYPE_PRIVATE
    }
}
