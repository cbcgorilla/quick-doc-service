package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.Authorization
import cn.mxleader.quickdoc.entities.SysDisk

data class WebDisk(val id: String, var name: String, var authorizations: Authorization? = null){
    constructor(disk:SysDisk):this(disk.id.toString(),disk.name,disk.authorization)
}