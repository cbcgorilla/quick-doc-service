package cn.mxleader.quickdoc.security.exp;

import org.springframework.security.core.AuthenticationException;

public class UserLogonException extends AuthenticationException {
    public UserLogonException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserLogonException(String msg) {
        super(msg);
    }
}
