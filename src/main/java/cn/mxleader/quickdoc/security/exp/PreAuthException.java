package cn.mxleader.quickdoc.security.exp;

import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthTarget;
import org.bson.types.ObjectId;

public class PreAuthException extends RuntimeException {
    private AuthAction[] actions;
    private AuthTarget target;
    private ObjectId targetId;
    private String method;
    private String username;

    public PreAuthException(String msg, Throwable t) {
        super(msg, t);
    }

    public PreAuthException(String msg) {
        super(msg);
    }

    public PreAuthException(String msg, AuthAction[] actions, AuthTarget target,
                            ObjectId targetId, String method, String username) {
        super(msg);
        this.actions = actions;
        this.target = target;
        this.targetId = targetId;
        this.method = method;
        this.username = username;
    }

    public AuthAction[] getActions() {
        return actions;
    }

    public AuthTarget getTarget() {
        return target;
    }

    public String getTargetId() {
        return targetId.toString();
    }

    public String getMethod() {
        return method;
    }

    public String getUsername() {
        return username;
    }

    public String getPreAuthMessage() {
        return this.username + " :: " + this.actions.toString() + "::" + this.target.toString() + "("
                + this.targetId.toString() + ")" + this.getMessage();
    }
}
