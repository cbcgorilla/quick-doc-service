package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class SysDisk(@Id var id: ObjectId, var name: String, var authorizations: Set<Authorization>){

    fun addAuthorization(authorization: Authorization) {
        this.authorizations += authorization
    }

    fun removeAuthorization(authorization: Authorization) {
        this.authorizations -= authorization
    }

}