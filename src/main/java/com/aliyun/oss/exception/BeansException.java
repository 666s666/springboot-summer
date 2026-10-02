package com.aliyun.oss.exception;

public class BeansException extends NestedRuntimeException  {
    public BeansException(String message) {
        super(message);
    }

    public BeansException(Throwable cause) {
        super(cause);
    }

    public BeansException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeansException() {
    }
}
