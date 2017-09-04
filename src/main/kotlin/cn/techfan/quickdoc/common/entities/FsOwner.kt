package cn.techfan.quickdoc.common.entities

data class FsOwner(var username: String, var type: Type, var privilege:Int=1){
    enum class Type {
        TYPE_PUBLIC, TYPE_PRIVATE
    }
}
