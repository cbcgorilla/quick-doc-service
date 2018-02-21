package cn.mxleader.quickdoc.common;

public class QuickDocException extends RuntimeException {
    private static final long serialVersionUID = 345365998095354380L;

    public QuickDocException() {
        super();
    }

    public QuickDocException(String message) {
        super(message);
    }
}
