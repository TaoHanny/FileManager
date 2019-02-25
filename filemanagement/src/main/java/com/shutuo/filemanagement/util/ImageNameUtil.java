package com.shutuo.filemanagement.util;

public class ImageNameUtil {

    public static String getLastName(String name){
        String filename = getFileName(name);
        String caselsh = filename.substring(0,filename.lastIndexOf("."));
        return caselsh;
    }

    public static String getTyle(String name){
        String fileName = getFileName(name);
        String fileTyle=fileName.substring(fileName.lastIndexOf("."),fileName.length());
        return fileTyle;
    }

    private static String getFileName(String name){
//        String fileNameNow = name.substring(name.lastIndexOf("\\")+1);
        String temp[]=name.split("\\\\");
        String fileNameNow=temp[temp.length-1];
        return fileNameNow;
    }
}
