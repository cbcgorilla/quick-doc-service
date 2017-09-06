package cn.techfan.quickdoc.common.exception;

public class InvalidPasswordException extends RuntimeException {

    private static final long serialVersionUID = 1391232092006287163L;

    public InvalidPasswordException(final String message) {
        super(message);
    }

}
