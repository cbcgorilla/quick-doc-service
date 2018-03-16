package cn.mxleader.quickdoc.entities

data class Metadata(var _contentType: String,
                    var parents: List<ParentLink>? = null,
                    var authorizations: List<AccessAuthorization>? = null,
                    var labels: List<String>? = null) {
    constructor() : this("application/octet-stream", null, null, null)
}