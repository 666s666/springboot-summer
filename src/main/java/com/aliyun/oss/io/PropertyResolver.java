package com.aliyun.oss.io;

import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.util.*;
import java.util.function.Function;

record PropertyExpr(String key,String defaultValue){};
public class PropertyResolver {
    Map<String,String> properties = new HashMap<>();
    Map<Class<?>, Function<String,Object>> converters = new HashMap<>();
    public PropertyResolver(Properties pros){
        this.properties.putAll(System.getenv());
        Set<String> names = pros.stringPropertyNames();
        for(String name : names){
            this.properties.put(name,pros.getProperty(name));
        }
        converters.put(String.class, s -> s);
        converters.put(boolean.class, s -> Boolean.parseBoolean(s));
        converters.put(Boolean.class, s -> Boolean.valueOf(s));

        converters.put(byte.class, s -> Byte.parseByte(s));
        converters.put(Byte.class, s -> Byte.valueOf(s));

        converters.put(short.class, s -> Short.parseShort(s));
        converters.put(Short.class, s -> Short.valueOf(s));

        converters.put(int.class, s -> Integer.parseInt(s));
        converters.put(Integer.class, s -> Integer.valueOf(s));

        converters.put(long.class, s -> Long.parseLong(s));
        converters.put(Long.class, s -> Long.valueOf(s));

        converters.put(float.class, s -> Float.parseFloat(s));
        converters.put(Float.class, s -> Float.valueOf(s));

        converters.put(double.class, s -> Double.parseDouble(s));
        converters.put(Double.class, s -> Double.valueOf(s));

        converters.put(LocalDate.class, s -> LocalDate.parse(s));
        converters.put(LocalTime.class, s -> LocalTime.parse(s));
        converters.put(LocalDateTime.class, s -> LocalDateTime.parse(s));
        converters.put(ZonedDateTime.class, s -> ZonedDateTime.parse(s));
        converters.put(Duration.class, s -> Duration.parse(s));
        converters.put(ZoneId.class, s -> ZoneId.of(s));

    }
    @Nullable
    PropertyExpr parsePropertyExpr(String key){
        if(key.startsWith("${")&&key.endsWith("}")){
            int n = key.indexOf(':');
            if(n==-1){
                return new PropertyExpr(key.substring(2,key.length()-1),null);
            }
            String k = key.substring(2,n);
            return new PropertyExpr(k,key.substring(n+1,key.length()-1));

        }
        return null;
    }
    @Nullable
    String getProperty(String key){
        PropertyExpr propertyExpr = parsePropertyExpr(key);

            if (propertyExpr != null) {
                if (propertyExpr.defaultValue() != null) {

                    return getProperty(propertyExpr.key(), propertyExpr.defaultValue());
                } else {
                    return getRequireProperty(propertyExpr.key());

                }
            }


        String k = this.properties.get(key);
            if(k!=null){
                return parseValue(k);
            }
            return k ;

    }
    String getRequireProperty(String key){
        String value = getProperty(key);
        if(value !=null){
            return parseValue(value);
        }
        return value;
    }
    String getProperty(String key,String defaultValue){
        String value = parseValue(key);
        return value==null?parseValue(defaultValue):value;
    }
    String parseValue(String key){
        PropertyExpr keyExpr = parsePropertyExpr(key);
       if(keyExpr!=null){
           if(keyExpr.defaultValue()!=null){
               return getProperty(keyExpr.key(),keyExpr.defaultValue());
           }
           return getRequireProperty(keyExpr.key());
       }
        return key;
    }
    public <T> T getProperty(String key,Class<T> type){
       String value = getProperty(key);
       if(value==null) return null;
       return convert(type,key);
    }
    public boolean containsProperty(String key) {
        return this.properties.containsKey(key);
    }
    public <T> T getProperty(String key,Class<T> type,T defaultValue){
        T value = getProperty(key,type);
        if(value==null){
            return defaultValue;
        }
        return value;
    }

    String notEmpty(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return key;
    }
    public <T> T getRequireProperty(String key,Class<T> type){
        T value = getProperty(key,type);

        return Objects.requireNonNull(value,"Property '" + key + "' not found.");
    }
    public <T> T convert(Class<T> type, String value){
        Function<String,Object> f = converters.get(type);
        if(f==null){
            throw new IllegalArgumentException("Unsupported value type: " + type.getName());
        }
        return (T)f.apply(value);
    }
}
