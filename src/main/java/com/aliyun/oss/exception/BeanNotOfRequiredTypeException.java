package com.aliyun.oss.exception;

public class BeanNotOfRequiredTypeException extends BeansException {
    public BeanNotOfRequiredTypeException(String message) {
        super(message);

    }

    public BeanNotOfRequiredTypeException(Throwable cause) {
        super(cause);
    }

    public BeanNotOfRequiredTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BeanNotOfRequiredTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanNotOfRequiredTypeException() {
    }
}
