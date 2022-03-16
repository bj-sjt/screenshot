package com.itao.screenshot.util;

import java.io.InputStream;
import java.net.URL;

public class ResourceUtil {

    public static URL getResource(String path){
        URL url = ResourceUtil.class.getResource(path);
        if (url == null) {
            throw new NullPointerException();
        }
        return url;
    }

    public static InputStream getResourceAsStream(String path){
        InputStream stream = ResourceUtil.class.getResourceAsStream(path);
        if (stream == null) {
            throw new NullPointerException();
        }
        return stream;
    }

    public static String getPath(String path){
        URL url = getResource(path);
        return url.getPath();
    }

    public static String getFile(String path){
        URL url = getResource(path);
        return url.getFile();
    }

    public static String getExternal(String path){
        URL url = getResource(path);
        return url.toExternalForm();
    }

}
