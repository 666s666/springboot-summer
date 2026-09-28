package com.aliyun.oss;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ResourceResolver {
    String basePackage;
    Logger logger = LoggerFactory.getLogger(getClass());
    public ResourceResolver(String basePackage){
        this.basePackage = basePackage;
    }
    public <R> List<R> scan(Function<Resource,R> mapper) {
        String basePackagePath = this.basePackage.replace(".","/");
        String path = basePackagePath;
        try{
            List<R> collector = new ArrayList<>();
            scan0(basePackagePath,path,collector,mapper);
            return collector;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }

    }
    private <R> void scan0(String basePackagePath,String path,List<R> collector,Function<Resource,R> mapper) throws IOException, URISyntaxException {
        logger.atDebug().log("scan path:{}",path);
        Enumeration<URL> en = getContextClassLoader().getResources(path);
        while(en.hasMoreElements()){
            URL url = en.nextElement();
            URI uri = url.toURI();
            String uristr = removeTrailingSlash(uri.toString());
            String uribasestr = uristr.substring(0,uristr.length()-basePackagePath.length());
            if(uribasestr.startsWith("file:")){
                uribasestr = uristr.substring(5);
            }
            if(uribasestr.startsWith("jar:")){
                scanFile(true,uribasestr,jarUriToPath(basePackagePath,uri),collector,mapper);
            }else{
                scanFile(false,uribasestr, Paths.get(uri),collector,mapper);
            }
        }
    }
    ClassLoader getContextClassLoader(){
        ClassLoader c = Thread.currentThread().getContextClassLoader();
        if(c == null){
            c = getClass().getClassLoader();
        }
        return c;
    }
    Path jarUriToPath(String basePackage, URI jarUri) throws IOException{
        return FileSystems.newFileSystem(jarUri, Map.of()).getPath(basePackage);
    }
    <R> void scanFile(boolean isJar,String base,Path root,List<R> collector,Function<Resource,R> mapper) throws IOException{
        String baseDir = removeTrailingSlash(base);
        Files.walk(root).filter(Files::isRegularFile).forEach(file ->{
            Resource res = null;
            if(isJar){
                res = new Resource(baseDir,removeLeadingSlash(file.toString()));
            }else{
                String path = file.toString();
                String name = removeLeadingSlash(path.substring(baseDir.length()));
                res = new Resource("file:"+path,name);
            }
            R r = mapper.apply(res);
            if(r != null){
                collector.add(r);
            }
        });

    }
    String removeTrailingSlash(String path){
        if(path.endsWith("/")||path.endsWith("\\")){
            path = path.substring(0,path.length()-1);
        }
        return path;
    }
    String removeLeadingSlash(String path){
        if(path.startsWith("/")||path.startsWith("\\")){
            return path.substring(1);
        }
        return path;
    }

}
