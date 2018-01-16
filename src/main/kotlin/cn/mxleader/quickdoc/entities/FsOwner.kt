package cn.mxleader.quickdoc.entities

data class FsOwner(val username: String, var type: Type, var privilege: Int) {
    enum class Type {
        TYPE_PUBLIC, TYPE_PRIVATE
    }
}