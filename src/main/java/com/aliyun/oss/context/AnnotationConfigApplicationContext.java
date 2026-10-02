package com.aliyun.oss.context;

import com.aliyun.oss.annotation.*;
import com.aliyun.oss.exception.*;
import com.aliyun.oss.io.PropertyResolver;
import com.aliyun.oss.io.ResourceResolver;
import com.aliyun.oss.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.beans.beancontext.BeanContextServiceProviderBeanInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final PropertyResolver propertyResolver;
    protected final Map<String,BeanDefinition> beans;

    public AnnotationConfigApplicationContext(PropertyResolver propertyResolver,Class<?> configuration) {
        this.propertyResolver = propertyResolver;
        Set<String> beanClassName = scanForClassNames(configuration);
        this.beans = createBeanDefinitions(beanClassName);
    }

    /**
     * 根据扫描的ClassName创建BeanDefinition
     * @param beanClassName
     * @return
     */
    private Map<String,BeanDefinition> createBeanDefinitions(Set<String> beanClassName){
        Map<String, BeanDefinition> map = new HashMap<>();
        for(String className:beanClassName){
            Class<?> clazz = null;
            try{
                clazz = Class.forName(className);
            }catch(ClassNotFoundException e){
                throw new BeanCreationException(e);
            }
            if(clazz.isAnnotation()||clazz.isEnum()||clazz.isRecord()||clazz.isInterface()){
                continue;
            }
            Component component = ClassUtils.findAnnotation(clazz,Component.class);
            if(component!=null){
                logger.atDebug().log("found component :{}" + clazz.getName());
                int mod = clazz.getModifiers();
                if(Modifier.isAbstract(mod)){
                    throw new BeanDefinitionException("@Component class" + clazz.getName() + "must not be abstract");

                }
                if(Modifier.isPrivate(mod)){
                    throw new BeanDefinitionException("@Component class" + clazz.getName() + "must not be private");
                }
                String beanName = ClassUtils.getBeanName(clazz);
                BeanDefinition bdf = new BeanDefinition(beanName,clazz,getSuitableConstructor(clazz),getOrder(clazz),clazz.isAnnotationPresent(Primary.class),
                        null,null,ClassUtils.findAnnotationMethod(clazz,PostConstruct.class),
                        ClassUtils.findAnnotationMethod(clazz,PreDestroy.class));
                        addBeanDefinition(map,bdf);
                        Configuration configuration = ClassUtils.findAnnotation(clazz,Configuration.class);
                        if(configuration != null){
                            scanFactoryMethods(beanName,clazz,map);
                        }

            }

        }
        return map;
    }

    /**
     * Get public constructor or non-public constructor ad fallback
     * @param clazz
     * @return
     */
    Constructor<?> getSuitableConstructor(Class<?> clazz){
        Constructor<?>[] cons = clazz.getConstructors();
        if(cons.length==0){
            cons = clazz.getDeclaredConstructors();
            if(cons.length != 1){
                throw new BeanDefinitionException();
            }

        }
        if(cons.length!=1){
            throw new BeanDefinitionException();
        }
        return cons[0];
    }
    void scanFactoryMethods(String factoryBeanName,Class<?> clazz , Map<String,BeanDefinition> map){
        for(Method method : clazz.getMethods()){
           Bean bean = method.getAnnotation(Bean.class);
           if(bean != null) {
               int mod = method.getModifiers();
               if (Modifier.isAbstract(mod)) {
                   throw new BeanDefinitionException("@Bean method " + method.getName() + "must not be abstract");
               }
               if (Modifier.isPrivate(mod)) {
                   throw new BeanDefinitionException("@Bean method " + method.getName() + "must not be private");
               }
               if (Modifier.isFinal(mod)) {
                   throw new BeanDefinitionException("@Bean method" + method.getName() + " must not be final");
               }
               Class<?> beanClass = method.getReturnType();
               if(beanClass.isPrimitive()){
                   throw new BeanDefinitionException("@Bean method " + method.getName()+"must not return primitive ");

               }
               if(beanClass==void.class||beanClass==Void.class){
                   throw new BeanDefinitionException("@Bean method " + method.getName() + "must not return empty");

               }
               BeanDefinition bdf = new BeanDefinition(ClassUtils.getBeanName(beanClass),beanClass,factoryBeanName,method,getOrder(method),method.isAnnotationPresent(Primary.class),bean.initMethod().isEmpty()?null:bean.initMethod(),bean.destroyMethod().isEmpty()?null:bean.destroyMethod(),null,null);
               addBeanDefinition(map,bdf);
               logger.atDebug().log("define bean: {}", bdf);
           }

        }
    }
    void addBeanDefinition(Map<String,BeanDefinition> map,BeanDefinition bdf){
        if(map.put(bdf.getName(),bdf)!=null){
            throw new BeanDefinitionException("Duplicate bean name" + bdf.getName());
        }
    }
    int getOrder(Method method){
        Order order = method.getAnnotation(Order.class);
        if(order == null){
            return Integer.MAX_VALUE;
        }
        return order.value();

    }
    int getOrder(Class<?> clazz){
        Order order = clazz.getAnnotation(Order.class);
        return order==null?Integer.MAX_VALUE:order.value();
    }
    Set<String> scanForClassNames(Class<?> configuration){
        Set<String> classNameSet = new HashSet<>();
        ComponentScan scan = configuration.getAnnotation(ComponentScan.class);
        final String[] packages = scan==null||scan.value().length==0?new String[]{configuration.getPackageName()}:scan.value();
        logger.atInfo().log("component scan in packages: {}", Arrays.toString(packages));
        for(String pkg: packages ){
            var rr = new ResourceResolver(pkg);
            List<String> classList = rr.scan(res->{
                String name = res.name();
                if(name.endsWith(".class")){
                    return name.substring(0,name.length()-6).replace("/",".").replace("\\",".");

                }
                return null;
            });
                if(logger.isDebugEnabled()){
                    classList.forEach(className ->{
                        logger.debug("class found by component scan :{}",className);
                    });
                }
                classNameSet.addAll(classList);



        }
        Import imports = configuration.getAnnotation(Import.class);
        if(imports != null){
            for(Class<?> c : imports.value()){
                String className = c.getName();
                if(classNameSet.contains(className)){
                    logger.warn("ignore import :{}",className);
                }
                else{
                    logger.debug("class found by import:{}",className);
                    classNameSet.add(className);
                }
            }

        }
        return classNameSet;
    }
    boolean isConfigurationBeanDefinition(BeanDefinition bd){
        return ClassUtils.findAnnotation(bd.getBeanClass(),Configuration.class)!=null;
    }
    BeanDefinition findBeanDefinition(String name){
        return this.beans.get(name);
    }
    BeanDefinition findBeanDefinition(String name,Class<?> type){
        BeanDefinition bd = findBeanDefinition(name);
        if(bd ==null){
            return null;
        }
        if(!type.isAssignableFrom(bd.getBeanClass())){
            throw new BeanNotOfRequiredTypeException(String.format("Autowire required type '%s' but bean '%s' has actual type '%s'.", type.getName(),
                    name, bd.getBeanClass().getName()));
        }
        return bd;
    }
    public List<BeanDefinition> findBeanDefinition(Class<?> type){
        return this.beans.values().stream().filter(res -> type.isAssignableFrom(res.getBeanClass())).sorted().collect(Collectors.toList());
    }
    @Nullable
    public BeanDefinition findBeanDefinition(Class<?> type){
        List<BeanDefinition> list = findBeanDefinition(type);
        if(list.isEmpty()){
            return null;
        }
        if(list.size()==1){
            return list.get(0);
        }
        List<BeanDefinition> bds = list.stream().filter(res ->
           res.isPrimary()).collect(Collectors.toList());
        if(bds.size()==1){
            return bds.get(0);
        }
        if (bds.isEmpty()) {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        } else {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
        }

    }

}
