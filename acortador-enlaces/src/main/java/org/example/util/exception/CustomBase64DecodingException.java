package org.example.util.exception;

public class CustomBase64DecodingException extends Exception {
    public CustomBase64DecodingException() {
        super();
    }
    public CustomBase64DecodingException(String message) {
        super(message);
    }
    public CustomBase64DecodingException(String message, Throwable cause) {
        super(message, cause);
    }
    public CustomBase64DecodingException(Throwable cause) {
        super(cause);
    }
    protected CustomBase64DecodingException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
