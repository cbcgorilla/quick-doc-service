package cn.mxleader.quickdoc.entities

data class Metadata(var _contentType: String,
                    var parents: Set<ParentLink>,
                    var authorizations: Set<Authorization>,
                    var labels: Set<String>) {
    constructor() : this("application/octet-stream", emptySet(), emptySet(), emptySet())

    fun addAuthorization(authorization: Authorization) {
        this.authorizations += authorization
    }

    fun removeAuthorization(authorization: Authorization) {
        this.authorizations -= authorization
    }

}