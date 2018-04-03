package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.AuthAction
import cn.mxleader.quickdoc.entities.AuthType
import cn.mxleader.quickdoc.entities.Authorization
import cn.mxleader.quickdoc.entities.SysDisk

data class WebDisk(val id: String, var name: String, var owner: String,
                   var ownerType: AuthType, var authActions: Set<AuthAction>) {
    constructor(disk: SysDisk) : this(disk.id.toString(), disk.name, disk.authorization.name,
            disk.authorization.type, disk.authorization.actions)
}