package com.aliyun.oss.exception;

public class BeanCreationException extends BeansException {
    public BeanCreationException() {
    }

    public BeanCreationException(String message) {
        super(message);
    }
    public BeanCreationException(String message,Throwable cause){
        super(message,cause);
    }
    public BeanCreationException(Throwable cause){
        super(cause);
    }

    public BeanCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
