package com.aliyun.oss.utils;

import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlUtils {
    @SuppressWarnings("unchecked")
    public static Map<String,Object> loadYaml(String path) {
        var loaderOption = new LoaderOptions();
        var dumperOption = new DumperOptions();
        var representer = new Representer(dumperOption);
        var resolver = new NoImplicitResolver();
        var yaml = new Yaml(new Constructor(loaderOption), representer, dumperOption, loaderOption, resolver);
        return ClassPathUtils.readInputStream(path, (input) -> {
            return (Map<String, Object>) yaml.load(input);
        });
    }
    public static Map<String,Object> loadYamlAsPlainMap(String path){
        Map<String,Object> data = loadYaml(path);
        Map<String,Object> plain = new LinkedHashMap<>();
        converTo(data,"",plain);
        return plain;
    }
    public static Map<String,Object> converTo(Map<String,Object> source,String prefix,Map<String,Object> plain){
        for(String key : source.keySet()){
            Object value = source.get(key);
            if(value instanceof Map){
                Map<String,Object> submap = (Map<String,Object>) value;
                converTo(submap,key+".",plain);
            }else if(value instanceof List){
                plain.put(prefix+key,value);
            }else{
                plain.put(prefix+key,value.toString());
            }
        }
        return plain;
    }

}
class NoImplicitResolver extends Resolver {
    public NoImplicitResolver(){
        super();
        super.yamlImplicitResolvers.clear();
    }
}