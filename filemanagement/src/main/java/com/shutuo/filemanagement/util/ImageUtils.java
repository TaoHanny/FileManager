package com.shutuo.filemanagement.util;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

public class ImageUtils {


    public static ArrayList<File> getImages(String path){

        File file = new File(path);
        ArrayList<File> fileList = new ArrayList<>();
        if(file.isDirectory()){
            Collections.addAll(fileList, new File(path).listFiles(imageFilter));
            return fileList;
        }
        return null;
    }

    private static FileFilter imageFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName();
            return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
        }
    };
}
