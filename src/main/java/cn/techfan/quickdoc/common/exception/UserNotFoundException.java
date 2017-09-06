package cn.techfan.quickdoc.common.exception;

public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 9208310032306287163L;

    public UserNotFoundException(final String message) {
        super(message);
    }

}
