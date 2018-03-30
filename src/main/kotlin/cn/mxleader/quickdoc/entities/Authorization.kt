package cn.mxleader.quickdoc.entities

data class Authorization(val name: String, var type: AuthType, var actions: Set<AuthAction>) {
    constructor(name: String, type: AuthType) : this(name, type, hashSetOf(AuthAction.READ))
    constructor(name: String, type: AuthType, action: AuthAction) : this(name, type, hashSetOf(action))

    fun add(action: AuthAction) {
        this.actions += action
    }

    fun remove(action: AuthAction) {
        this.actions -= action
    }
}