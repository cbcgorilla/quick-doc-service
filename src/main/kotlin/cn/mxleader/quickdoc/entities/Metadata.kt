package cn.mxleader.quickdoc.entities

data class Metadata(var _contentType: String,
                    var parents: Array<ParentLink>? = null,
                    var authorizations: Array<AccessAuthorization>? = null,
                    var labels: Array<String>? = null) {
    constructor() : this("application/octet-stream", null, null, null)
}