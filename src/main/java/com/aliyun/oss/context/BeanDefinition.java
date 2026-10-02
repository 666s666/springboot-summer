package com.aliyun.oss.context;

import com.aliyun.oss.exception.BeanCreationException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class BeanDefinition implements Comparable<BeanDefinition>{
    private final String name;
    private final Class<?> beanClass;
    private Object instance;
    private final Constructor<?> constructor;
    private final String factoryName;
    private final Method factoryMethod;
    private final int order;
    private final boolean primary;
    private String initMethodName;
    private String destroyMethodName;
    private Method initMethod;
    private Method destroyMethod;
    public BeanDefinition(String name, Class<?> beanClass, Constructor<?> constructor, int order, boolean primary, String initMethodName,
                          String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = constructor;
        this.factoryName = null;
        this.factoryMethod = null;
        this.order = order;
        this.primary = primary;
        constructor.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }

    public BeanDefinition(String name, Class<?> beanClass, String factoryName, Method factoryMethod, int order, boolean primary, String initMethodName,
                          String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = null;
        this.factoryName = factoryName;
        this.factoryMethod = factoryMethod;
        this.order = order;
        this.primary = primary;
        factoryMethod.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }

    private void setInitAndDestroyMethod(String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.initMethodName = initMethodName;
        this.destroyMethodName = destroyMethodName;
        if (initMethod != null) {
            initMethod.setAccessible(true);
        }
        if (destroyMethod != null) {
            destroyMethod.setAccessible(true);
        }
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getInstance() {
        return instance;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public int getOrder() {
        return order;
    }

    public boolean isPrimary() {
        return primary;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public Method getInitMethod() {
        return initMethod;
    }

    public Method getDestroyMethod() {
        return destroyMethod;
    }
    public void setInstance(Object instance){
        Objects.requireNonNull(instance,"Bean instance is null");
        if(!this.beanClass.isAssignableFrom(instance.getClass())){
            throw new BeanCreationException(String.format("Instance '%s' of Bean '%s' is not the expected type: %s", instance, instance.getClass().getName(),
                    this.beanClass.getName()));
        }
        this.instance = instance;
    }
    @Override
    public String toString() {
        return "BeanDefinition [name=" + name + ", beanClass=" + beanClass.getName() + ", factory=" + getCreateDetail() + ", init-method="
                + (initMethod == null ? "null" : initMethod.getName()) + ", destroy-method=" + (destroyMethod == null ? "null" : destroyMethod.getName())
                + ", primary=" + primary + ", instance=" + instance + "]";
    }
    String getCreateDetail(){
        if(this.factoryMethod != null){
            String params = String.join(",", Arrays.stream(this.factoryMethod.getParameterTypes()).map(t -> t.getSimpleName()).toArray(String[]:: new));
            return this.factoryMethod.getDeclaringClass().getSimpleName()+"."+this.factoryMethod.getName()+"("+params+")";

        }
        return null;
    }
    @Override
    public int compareTo(@NotNull BeanDefinition o) {
        int cmp = Integer.compare(this.order,o.order);
        if(cmp != 0){
            return cmp;
        }

        return this.name.compareTo(o.name);
    }
}
