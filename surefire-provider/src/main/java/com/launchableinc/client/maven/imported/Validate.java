package com.launchableinc.client.maven.imported;

public class Validate {

    public static boolean isEmpty(String string){
        return string == null || string.trim().isEmpty();
    }

    public static boolean isNotEmpty(String string){
        return !isEmpty(string);
    }
}
