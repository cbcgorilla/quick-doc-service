package cn.mxleader.quickdoc.web.domain

data class TreeNode(val id: String, var name: String,
                    var parentId: String,
                    var children: List<TreeNode>,
                    var isParent: Boolean = true,
                    var open: Boolean = false,
                    var path: String,
                    var level: Int) {
    constructor(id: String,
                name: String,
                parentId: String,
                children: List<TreeNode>)
            : this(id, name, parentId, children, true, true, "", 0)

    constructor(id: String,
                name: String,
                parentId: String,
                children: List<TreeNode>,
                isParent: Boolean)
            : this(id, name, parentId, children, isParent, true, "", 0)

    fun getCompletePath():String = "$path-$name"

    fun addChildren(child: TreeNode) {
        child.parentId = this.id
        this.children += child
    }

    fun getChild(id: String): TreeNode? {
        for (child in children) {
            if (child.id == id)
                return child
        }
        return null
    }

}