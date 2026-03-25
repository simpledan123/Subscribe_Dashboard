package com.saas.subscriptionplatform.exception;

public class ApiLimitExceededException extends RuntimeException {
    public ApiLimitExceededException(String message) {
        super(message);
    }
}