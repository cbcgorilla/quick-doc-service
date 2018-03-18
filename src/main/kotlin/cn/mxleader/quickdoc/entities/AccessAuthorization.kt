package cn.mxleader.quickdoc.entities

data class AccessAuthorization(val name: String,
                               var type: Type,
                               var action: Action) {
    enum class Type {
        TYPE_GROUP, TYPE_PRIVATE
    }
    enum class Action {
        READ, WRITE, DELETE
    }
}