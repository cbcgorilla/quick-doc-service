package cn.mxleader.quickdoc.web.domain

data class FolderTreeNode(val id: String, var text: String,
                          var parentId: String,
                          var nodes: List<FolderTreeNode>)