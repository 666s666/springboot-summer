package com.aliyun.oss.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    /**
     * Bean name .defaule to method name
     */
    String value() default "";
    String initMethod() default "";
    String destroyMethod() default "";
}
