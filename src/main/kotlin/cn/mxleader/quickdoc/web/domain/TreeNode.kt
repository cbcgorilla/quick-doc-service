package cn.mxleader.quickdoc.web.domain

data class TreeNode(val id: String, var text: String,
                    var parentId: String,
                    var nodes: List<TreeNode>)