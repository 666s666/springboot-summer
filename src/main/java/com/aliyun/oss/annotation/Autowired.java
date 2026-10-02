package com.aliyun.oss.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    /**
     * is require
     *
     */
    boolean value() default true;

    /**
     *
     * Bean name if set
     */
    String name() default "";
}
