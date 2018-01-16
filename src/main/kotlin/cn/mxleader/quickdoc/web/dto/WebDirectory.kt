package cn.mxleader.quickdoc.web.dto

import cn.mxleader.quickdoc.entities.FsOwner

data class WebDirectory(var id: Long,
                        var path: String,
                        var parentId: Long,
                        var owners: Array<FsOwner>? = null,
                        var childrenCount: Long? = null) {
    constructor() : this(0, "", 0, null, null)
}