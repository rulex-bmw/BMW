package com.rulex.bmw.util;

/**
 * Custom exception
 */
public class DataException extends Exception {

    private String code;

    private static final long serialVersionUID = 1L;

    public DataException() {
        super();
    }

    public DataException(String code, String message) {
        super(message);
    }

    public DataException(String message) {
        super(message);
    }

    public String getCode() {
        return code;
    }

}

