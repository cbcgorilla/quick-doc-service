package cn.mxleader.quickdoc.web.session

data class ActiveUserStore(var users:List<String>) {
    constructor():this(emptyList<String>())

    fun addUser(username: String) {
        users += username
    }

    fun removeUser(username: String) {
        users -= username
    }
}