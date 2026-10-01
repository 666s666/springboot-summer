package com.aliyun.oss.utils;

import com.aliyun.oss.InputStreamCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class ClassPathUtils {
    public static <T> T readInputStream(String path, InputStreamCallback<T> inputstreamCallback){
        if(path.startsWith("/")){
            path = path.substring(1);
        }
        try(InputStream input = getContextClassLoader().getResourceAsStream(path)){
            if(input==null){
                throw new FileNotFoundException("file not found in classpath:"+path);
            }
            return inputstreamCallback.doWithInputStream(input);
        }catch(IOException e){
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }

    }
    static ClassLoader getContextClassLoader(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader!=null){
            return classLoader;
        }
        return ClassPathUtils.class.getClassLoader();
    }
    public static String readString(String path){
        return readInputStream(path,(input)->{
            byte[] data = input.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        });
    }
}
