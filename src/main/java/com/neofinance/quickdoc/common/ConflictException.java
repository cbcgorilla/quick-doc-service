package com.neofinance.quickdoc.common;

public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 1039829250633451756L;

    public ConflictException() {
        super();
    }

    public ConflictException(String s) {
        super(s);
    }
}
