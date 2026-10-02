package com.aliyun.oss.exception;

public class NoSuchBeanDefinitionException extends BeansException{
    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }

    public NoSuchBeanDefinitionException(Throwable cause) {
        super(cause);
    }

    public NoSuchBeanDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NoSuchBeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchBeanDefinitionException() {
    }
}
