package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId

data class ParentLink(val id: ObjectId, val type:PType){
    enum class PType {
        DISK, FOLDER
    }
}