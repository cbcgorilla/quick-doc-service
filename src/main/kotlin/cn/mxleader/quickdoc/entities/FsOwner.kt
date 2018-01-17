package cn.mxleader.quickdoc.entities

data class FsOwner(val name: String,
                   var type: Type,
                   var privilege: Int) {
    enum class Type {
        TYPE_PUBLIC, TYPE_GROUP, TYPE_PRIVATE
    }
}