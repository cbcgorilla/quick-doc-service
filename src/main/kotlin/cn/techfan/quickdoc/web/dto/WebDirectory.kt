package cn.techfan.quickdoc.web.dto

import cn.techfan.quickdoc.common.entities.FsOwner

data class WebDirectory(var id: Long,
                        var path: String,
                        var parentId: Long,
                        var owners: Array<FsOwner>? = null,
                        var subDirsCount:Int,
                        var filesCount:Int)