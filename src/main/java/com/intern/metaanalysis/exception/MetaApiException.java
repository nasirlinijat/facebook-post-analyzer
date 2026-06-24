package com.intern.metaanalysis.exception;

public class MetaApiException extends RuntimeException {

    public MetaApiException(String message) {
        super(message);
    }

    public MetaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
