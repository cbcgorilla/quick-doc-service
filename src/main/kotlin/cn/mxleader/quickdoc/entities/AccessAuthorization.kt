package cn.mxleader.quickdoc.entities

data class AccessAuthorization(val name: String,
                               var type: Type,
                               var privilege: Int) {
    enum class Type {
        TYPE_GROUP, TYPE_PRIVATE
    }
}