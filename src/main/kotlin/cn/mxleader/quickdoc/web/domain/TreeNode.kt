package cn.mxleader.quickdoc.web.domain

data class TreeNode(val id: String, var name: String,
                    var parentId: String,
                    var children: List<TreeNode>,
                    var isParent: Boolean = true,
                    var open: Boolean = false) {
    constructor(id: String,
                name: String,
                parentId: String,
                children: List<TreeNode>)
            : this(id, name, parentId, children, true, true)
    constructor(id: String,
                name: String,
                parentId: String,
                children: List<TreeNode>,
                isParent:Boolean)
            : this(id, name, parentId, children, isParent, true)
}