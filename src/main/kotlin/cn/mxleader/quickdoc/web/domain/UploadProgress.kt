package cn.mxleader.quickdoc.web.domain

data class UploadProgress(var bytesRead: Long,
                          var contentLength: Long,
                          var items: Long) {
    constructor() : this(0L, 0L, 0L)

    override fun toString(): String {
        return "Progress(bytesRead=$bytesRead, contentLength=$contentLength, items=$items)"
    }
}