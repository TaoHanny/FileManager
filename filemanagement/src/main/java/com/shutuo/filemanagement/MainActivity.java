package com.shutuo.filemanagement;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.mbms.DownloadProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.partition.Partition;
import com.shutuo.filemanagement.usbHelper.UsbHelper;
import com.shutuo.filemanagement.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //输入的内容
    private EditText u_disk_edt;
    //写入到U盘
    private Button u_disk_write;
    //从U盘读取
    private Button u_disk_read;
    //显示读取的内容
    private TextView u_disk_show;
    //显示第一张图片
    private ImageView u_disk_image;
    //自定义U盘读写权限
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //当前处接U盘列表
    private UsbMassStorageDevice[] storageDevices;
    //当前U盘所在文件目录
    private UsbFile cFolder;
    //当前U盘图片
    private File[] files;
    private UsbHelper usbHelper;
    private final static String U_DISK_FILE_NAME = "image";
    private final static String TAG = MainActivity.class.getName();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    showToastMsg("保存成功");
                    break;
                case 101:
                    String txt =  msg.obj.toString();
                    if (!TextUtils.isEmpty(txt))
                        u_disk_show.setText("读取到的数据是：" + txt);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_activity_main);
        initViews();
        registerUDiskReceiver();
    }

    private void initViews() {
        u_disk_edt = (EditText) findViewById(R.id.u_disk_edt);
        u_disk_write = (Button) findViewById(R.id.u_disk_write);
        u_disk_read = (Button) findViewById(R.id.u_disk_read);
        u_disk_show = (TextView) findViewById(R.id.u_disk_show);
        u_disk_image = (ImageView) findViewById(R.id.u_disk_image);
        u_disk_write.setOnClickListener(this);
        u_disk_read.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
            if(view.getId() == R.id.u_disk_write) {

                final String content = u_disk_edt.getText().toString().trim();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        saveText2UDisk(content);
                    }
                });
            }

            if (view.getId() == R.id.u_disk_read){

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        readFromUDisk();
                    }
                });
            }
    }

    private void readFromUDisk() {
        UsbFile[] usbFiles = new UsbFile[0];
        try {
            usbFiles = cFolder.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != usbFiles && usbFiles.length > 0) {
            for (UsbFile usbFile : usbFiles) {
                if (usbFile.getName().equals(U_DISK_FILE_NAME)) {
                    Log.d(TAG, "readFromUDisk: "+usbFile.getName());
                    readTxtFromUDisk(usbFile);
                }
            }
        }
    }

    /**
     * @description 保存数据到U盘，目前是保存到根目录的
     * @author ldm
     * @time 2017/9/1 17:17
     */
    private void saveText2UDisk(String content) {
        //项目中也把文件保存在了SD卡，其实可以直接把文本读取到U盘指定文件
        File file = FileUtil.getSaveFile(getPackageName()
                        + File.separator + FileUtil.DEFAULT_BIN_DIR,
                U_DISK_FILE_NAME);
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != cFolder) {
            FileUtil.saveSDFile2OTG(file, cFolder);
            mHandler.sendEmptyMessage(100);
        }
    }

    /**
     * @description OTG广播注册
     * @author ldm
     * @time 2017/9/1 17:19
     */
    private void registerUDiskReceiver() {
        //监听otg插入 拔出
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mOtgReceiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mOtgReceiver, filter);
    }

    /**
     * @description OTG广播，监听U盘的插入及拔出
     * @author
     * @time 2019/2/20 17:00
     * @param
     */
    private BroadcastReceiver mOtgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION://接受到自定义广播
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //允许权限申请
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            //用户已授权，可以进行读取操作
                            readDevice(getUsbMass(usbDevice));
                            Log.d(TAG, "onReceive: 接受自定义广播");
                        } else {
                            showToastMsg("没有插入U盘");
                        }
                    } else {
                        showToastMsg("未获取到U盘权限");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到U盘设备插入广播
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        //接收到U盘插入广播，尝试读取U盘设备数据
                        Log.d(TAG, "onReceive: 接受到U盘设备插入广播");
                        redUDiskDevsList();
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到U盘设设备拔出广播
                    showToastMsg("U盘已拔出");
                    break;
            }
        }
    };

    /**
     * @description U盘设备读取
     * @author ldm
     * @time 2017/9/1 17:20
     */
    private void redUDiskDevsList() {
        //设备管理器
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //获取U盘存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //一般手机只有1个OTG插口
        for (UsbMassStorageDevice device : storageDevices) {
            //读取设备是否有权限
            if (usbManager.hasPermission(device.getUsbDevice())) {
                Log.d(TAG, "redUDiskDevsList: 这个u盘有权限");
                readDevice(device);
            } else {
                //没有权限，进行申请
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        if (storageDevices.length == 0) {
            showToastMsg("请插入可用的U盘");
        }
    }

    private UsbMassStorageDevice getUsbMass(UsbDevice usbDevice) {
        for (UsbMassStorageDevice device : storageDevices) {
            if (usbDevice.equals(device.getUsbDevice())) {
                return device;
            }
        }
        return null;
    }

    private void readDevice(UsbMassStorageDevice device) {
        try {
            device.init();//初始化
            //设备分区
            Partition partition = device.getPartitions().get(0);
            //文件系统
            FileSystem currentFs = partition.getFileSystem();
            currentFs.getVolumeLabel();//可以获取到设备的标识
            //通过FileSystem可以获取当前U盘的一些存储信息，包括剩余空间大小，容量等等
            Log.e("Capacity: ", currentFs.getCapacity() + "");
            Log.e("Occupied Space: ", currentFs.getOccupiedSpace() + "");
            Log.e("Free Space: ", currentFs.getFreeSpace() + "");
            Log.e("Chunk size: ", currentFs.getChunkSize() + "");
            cFolder = currentFs.getRootDirectory();//设置当前文件对象为根目录
            Log.d(TAG, "readDevice: 读取device的信息");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void readTxtFromUDisk(UsbFile usbFile) {
        UsbFile descFile = usbFile;
        if(descFile.isDirectory()){
            Log.d(TAG, "readTxtFromUDisk: usbFile = "+usbFile.getName());
            try {
                UsbFile[] usbFiles = descFile.listFiles();
                for (UsbFile usbFile1 : usbFiles){
                    copyUSbFile(usbFile1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//                        
//                        files =  getImages("/"+usbFile1.getName());
//                        if (files.length != 0){
//                            u_disk_image.setImageURI(Uri.fromFile(files[1]));
//                        }
        //读取文件内容
//        InputStream is = new UsbFileInputStream(descFile);
//        //读取秘钥中的数据进行匹配
//        StringBuilder sb = new StringBuilder();
//        BufferedReader bufferedReader = null;
//        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(is));
//            String read;
//            while ((read = bufferedReader.readLine()) != null) {
//                sb.append(read);
//            }
//            String  rd = ""+sb;
//            if (rd !=null && !rd.isEmpty()){
//                Message msg = mHandler.obtainMessage();
//                msg.what = 101;
//                msg.obj = rd;
//                mHandler.sendMessage(msg);
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally {
//            try {
//                if (bufferedReader != null) {
//                    bufferedReader.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 复制 USB 文件到本地
     *
     * @param file USB文件
     */
    private void copyUSbFile(final UsbFile file) {
        //复制到本地的文件路径
        final String filePath = "/sdcard/arcFace/register" + File.separator + file.getName();
        Log.d(TAG, "copyUSbFile: filePath = "+ filePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //复制结果
                if(usbHelper==null){
                    Log.d(TAG, "run: usbhelper");
                }

                boolean result;
                try {
                    //开始写入
                    UsbFileInputStream uis = new UsbFileInputStream(file);//读取选择的文件的

                    FileOutputStream fos = new FileOutputStream(filePath);
                    //这里uis.available一直为0
//            int avi = uis.available();
                    long avi = file.getLength();
                    int writeCount = 0;
                    int bytesRead;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = uis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        writeCount += bytesRead;
//                Log.e(TAG, "Progress : write : " + writeCount + " All : " + avi);
//                        if (progressListener != null) {
//                            //回调下载进度
//                            progressListener.downloadProgress((int) (writeCount * 100 / avi));
//                        }
                    }
                    fos.flush();
                    uis.close();
                    fos.close();
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    result = false;
                }
//                return result;
//                final boolean result = usbHelper.saveUSbFileToLocal(file, filePath, new UsbHelper.DownloadProgressListener() {
//                    @Override
//                    public void downloadProgress(final int progress) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                String text = "From Usb "
//                                        + "\nTo Local " + "/sdcard/arcFace/register"
//                                        + "\n Progress : " + progress;
//                                Log.d(TAG, "run: text = "+text);
////                                showProgressTv.setText(text);
//                            }
//                        });
//                    }
//                });
//                //主线程更新UI
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (result) {
////                            openLocalFile(new File(localCurrentPath));
//                        } else {
//                            Toast.makeText(MainActivity.this, "复制失败",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
        }).start();
    }

    private File[] getImages(String path){
        Log.d(TAG, "getImages: ");
        File file = new File(path);
        Log.d(TAG, "getImages: file");
        if(file.isDirectory()){
            Log.d(TAG, "getImages: directory");
            File[] fs = file.listFiles(imageFilter);
            return fs;
        }
        return null;
    }

    private FileFilter imageFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
             String name = pathname.getName();
             return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mOtgReceiver);
    }
}
