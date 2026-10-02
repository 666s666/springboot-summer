package com.aliyun.oss.exception;

public class NoUniqueBeanDefinitionException extends BeanDefinitionException {
    public NoUniqueBeanDefinitionException() {
    }

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }

    public NoUniqueBeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoUniqueBeanDefinitionException(Throwable cause) {
        super(cause);
    }

    public NoUniqueBeanDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
