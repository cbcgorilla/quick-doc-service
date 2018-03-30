package cn.mxleader.quickdoc.entities

data class Metadata(var _contentType: String,
                    var parents: Set<ParentLink>? = null,
                    var authorizations: Set<Authorization>? = null,
                    var labels: Set<String>? = null) {
    constructor() : this("application/octet-stream", null, null, null)
}