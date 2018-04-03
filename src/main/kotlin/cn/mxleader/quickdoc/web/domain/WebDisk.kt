package cn.mxleader.quickdoc.web.domain

import cn.mxleader.quickdoc.entities.SysDisk

data class WebDisk(val id: String, var name: String) {
    constructor(disk: SysDisk) : this(disk.id.toString(), disk.name)
}