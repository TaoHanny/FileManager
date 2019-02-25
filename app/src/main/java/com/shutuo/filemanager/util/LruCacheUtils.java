package com.shutuo.filemanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;


/**
 * 图片缓存类（仅供缓存本地图片使用）
 */
public class LruCacheUtils {

    private final static String TAG = LruCacheUtils.class.getName();
    //sdcard指定存放缓存图片文件绝对路径
    private final static String IMAGE_LOAD_PATH = "/sdcard/arcFace/register/load";



    private static LruCache<String, Bitmap> mMemoryCache;

    /**
     * 初始化（必须），推荐在Application中启动，一次就可以了
     */
    public static void init() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        // 使用最大可用内存值的1/8作为缓存的大小。
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap bitmap) {
                //在每次存入缓存的时候调用
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    /**
     * 将bitmap加入到缓存中
     *
     * @param path   LruCache的键，即图片的下载路径
     * @param bitmap LruCache的值，即图片的Bitmap对象
     */
    public static void addImage(String path, Bitmap bitmap) {
        if (mMemoryCache.get(path) == null) {
            mMemoryCache.put(path, bitmap);
        }
    }

    /**
     * 读取缓存中的图片，不需要压缩
     *
     * @param path
     * @return
     */
    public static void getImage(String path) {
//        Bitmap bitmap = mMemoryCache.get(path);
//        if (bitmap != null) {
//            return bitmap;
//        }
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        if (bitmap != null) {
//            addImage(path, bitmap);
//        }
//        return bitmap;
    }

    /**
     * 读取缓存中的图片，需要压缩
     *
     * @param path
     * @return
     */
    public static Bitmap getImage(Context context, final String path) {
        Log.d(TAG, "getImage: path = "+path);
        Bitmap bitmap = mMemoryCache.get(path);
        if (bitmap != null) {
            //充缓存里面读取
            return bitmap;
        }
        //缓存里面还没有
        bitmap = BitmapFactory.decodeFile(path);//直接从文件中读取Bitmap出来
        if (bitmap != null) {
            String cachePath = IMAGE_LOAD_PATH;//压缩之后的文件存放的文件夹，自己设置
            Luban.with(context)
                    .load(path)
                    .ignoreBy(10)//不压缩的阈值，单位为K
                    .setTargetDir(cachePath)
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        @Override
                        public void onSuccess(File file) {
                            // TODO 压缩成功后调用，返回压缩后的图片文件
                            Bitmap bitmap1 = BitmapFactory.decodeFile(file.getPath());
                            Log.e(TAG,"压缩成功：" + file.length());
                            addImage(path, bitmap1);

                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO 当压缩过程出现问题时调用
//                            ALog.e("压缩出错：" + e.toString());
                            addImage(path, BitmapFactory.decodeFile(path));//压缩失败，直接缓存（很少出现）
                        }
                    }).launch();
        }
        return bitmap;
    }



        /**
         * 遍历文件夹下的文件
         *
         * @param file 地址
         */
    public static List<File> getFile(File file) {
        List<File> list = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return null;
        } else {
            for (File f : fileArray) {
                if (f.isFile()) {
                    list.add(0, f);
                } else {
                    getFile(f);
                }
            }
        }
        return list;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件地址
     * @return
     */
    public static boolean deleteFiles(String filePath) {
        List<File> files = getFile(new File(filePath));
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);

                /**  如果是文件则删除  如果都删除可不必判断  */
                if (file.isFile()) {
                    file.delete();
                }

            }
        }
        return true;
    }



}
