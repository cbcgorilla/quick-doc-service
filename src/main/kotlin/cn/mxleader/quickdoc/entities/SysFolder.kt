package cn.mxleader.quickdoc.entities

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class SysFolder(@Id var id: ObjectId,
                     var name: String,
                     var parents: Set<ParentLink>,
                     var authorizations: Set<Authorization>) {

    fun firstParent(): ParentLink = this.parents.first()

    fun addParent(parent: ParentLink) {
        this.parents += parent
    }

    fun removeParent(parent: ParentLink) {
        this.parents -= parent
    }

    fun addAuthorization(authorization: Authorization) {
        this.authorizations += authorization
    }

    fun removeAuthorization(authorization: Authorization) {
        this.authorizations -= authorization
    }
}