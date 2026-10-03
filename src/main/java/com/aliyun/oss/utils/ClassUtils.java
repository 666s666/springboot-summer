package com.aliyun.oss.utils;

import com.aliyun.oss.annotation.Bean;
import com.aliyun.oss.annotation.Component;
import com.aliyun.oss.exception.BeanDefinitionException;
import sun.reflect.ReflectionFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> target, Class<A> annonClass){
        A a = target.getAnnotation(annonClass);
        for(Annotation annot : annonClass.getAnnotations()){
            Class<? extends Annotation> type = annot.annotationType();
            if(!type.getPackageName().equals("com.aliyun.oss.annotation")){
                A found = findAnnotation(type,annonClass);
                if(found!=null){
                    if(a != null){
                        throw new BeanDefinitionException("Duplicate @"+annonClass.getSimpleName()+"found on class"+target.getSimpleName());

                    }
                    a = found;

                }
            }
        }
        return a;
    }
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getAnnotation(Annotation[] annons,Class<A> annonClass){
        for(Annotation ann : annons){
            if(annonClass.isInstance(ann)){
                return (A) ann;
            }
        }
        return null;
    }
    public static String getBeanName(Method method){
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value();
        if(name.isEmpty()){
            name = method.getName();
        }
        return name;



    }
    public static String getBeanName(Class<?> clazz){
        String name = "";
        Component component = clazz.getAnnotation(Component.class);
        if(component != null){
            name = component.value();
        }else{
            for(Annotation annon : clazz.getAnnotations()){
                if(findAnnotation(annon.getClass(),Component.class)!=null){
                    try{
                        name = (String) annon.getClass().getMethod("value").invoke(annon);
                    }catch(ReflectiveOperationException e){
                        throw new BeanDefinitionException("Cannot get annotation value.",e);
                    }
                }


            }
        }
        if(name.isEmpty()){
            name = clazz.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }
    public static Method findAnnotationMethod(Class<?> clazz,Class<? extends Annotation> annonClass){
        List<Method> ms = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annonClass)).map(m->{
            if(m.getParameterCount()!=0){
                throw new BeanDefinitionException( String.format("Method '%s' with @%s must not have argument: %s", m.getName(), annonClass.getSimpleName(), clazz.getName()));
            }
            return m;
        }).collect(Collectors.toList());
        if(ms.isEmpty()){
            return null;
        }
        if(ms.size()==1){
            return ms.get(0);
        }
        throw new BeanDefinitionException(String.format("Multiple methods with @%s found in class: %s", annonClass.getSimpleName(), clazz.getName()));



    }
    /**
     * Get non-arg method by method name.Not search in super class.
     */
    public static Method getNamedMethod(Class<?> clazz,String method){
        try{
            return clazz.getDeclaredMethod(method);
        }catch(ReflectiveOperationException e){
            throw new BeanDefinitionException(String.format("Method '%s' not found in class: %s", method, clazz.getName()));
        }
    }
}
