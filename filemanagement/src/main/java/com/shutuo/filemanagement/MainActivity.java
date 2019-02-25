package com.shutuo.filemanagement;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.partition.Partition;
import com.shutuo.filemanagement.adapter.FileListAdapter;
import com.shutuo.filemanagement.usbHelper.USBBroadCastReceiver;
import com.shutuo.filemanagement.usbHelper.UsbHelper;
import com.shutuo.filemanagement.util.ImageNameUtil;
import com.shutuo.filemanagement.util.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener   {


    //显示
    private Button u_disk_write;
    //拷贝U盘数据
    private Button u_disk_read;
    //显示读取的内容
    private TextView u_disk_show;
    //显示图片
    private ListView u_disk_listview;
    //自定义U盘读写权限
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //当前处接U盘列表
    private UsbMassStorageDevice[] storageDevices;
    //广播
    private USBBroadCastReceiver receiver;
    //sdcard指定存放图片文件绝对路径
    private final static String IMAGE_FILE_PATH = "/sdcard/arcFace/register";
    //当前U盘所在文件目录
    private UsbFile cFolder;
    //当前U盘图片
    private File[] files;
    //当前图片的本地存储路径列表
    private List<File> imageList;
    private FileListAdapter adapter;
    //USB copy文件工具
    private UsbHelper usbHelper;
    //U盘图片文件指定存放路径
    private final static String U_DISK_FILE_NAME = "image";
    private final static String TAG = MainActivity.class.getName();
    //初始化对话框布局文件
    AlertDialog.Builder customizeDialog ;

    public static boolean OK = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    int count = msg.arg1;
                    int add = msg.arg2;
                    if (add != 0&& count!=add){

                        u_disk_show.setText("正在复制： "+count+" / "+add);
                        imageList.clear();
                        imageList.addAll(ImageUtils.getImages(IMAGE_FILE_PATH))  ;
                        if (imageList==null && imageList.size()==0){
                            Log.d(TAG, "handleMessage: imageList = null");
                        }
                        adapter.notifyDataSetChanged();

                            Log.d(TAG, "handleMessage: count = "+ count+" ; add = "+add);
                        if(OK){
                            showToastMsg("已完成拷贝");
                            u_disk_write.setVisibility(View.VISIBLE);
//                            adapter.notifyDataSetChanged();
                            OK = true;
                        }
                    }else {
                        u_disk_show.setText("");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_activity_main);
        initViews();
        if (receiver==null){

            registerUDiskReceiver();

        }

    }

    private void initViews() {

        u_disk_write = (Button) findViewById(R.id.u_disk_write);
        u_disk_read = (Button) findViewById(R.id.u_disk_read);
        u_disk_show = (TextView) findViewById(R.id.u_disk_show);
        u_disk_listview = (ListView) findViewById(R.id.u_disk_listview);
        u_disk_write.setOnClickListener(this);
        u_disk_read.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        customizeDialog = new AlertDialog.Builder(MainActivity.this);


        imageList = ImageUtils.getImages(IMAGE_FILE_PATH);

        if (imageList.size()!=0 && imageList!=null){
            u_disk_write.setVisibility(View.VISIBLE);
            Log.d(TAG, "onStart: GONE");
        }else {
            u_disk_write.setVisibility(View.GONE);
        }

        adapter = new FileListAdapter(this,imageList);
        u_disk_listview.setAdapter(adapter);
        Log.d(TAG, "onStart: 已执行");
        u_disk_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String path = imageList.get(position).getName();
                showCustomizeDialog(path,position);
                return false;
            }
        });

        while (imageList==null){
            imageList = ImageUtils.getImages(IMAGE_FILE_PATH);
            Log.d(TAG, "onStart:  notify");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();


    }

    @Override
    public void onClick(View view) {
            if(view.getId() == R.id.u_disk_write) {
                Intent intent = new Intent(MainActivity.this,com.shutuo.face.register.MainActivity.class);
                startActivity(intent);
                OK = false;
            }
            if (view.getId() == R.id.u_disk_read){

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (cFolder != null){
                            if (!readFromUDisk()){
                                showToastMsg("请将图片存放到根目录文件： "+U_DISK_FILE_NAME+" 文件夹");
                            }
                        }else {
                            showToastMsg("未检测到U盘，请重新插入");
                        }
                    }
                });
            }
    }

    private void showCustomizeDialog(final String path, final int position) {
        final View dialogView; dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_customize,null);
        customizeDialog.setTitle("图片重命名");
        customizeDialog.setView(dialogView);
        final EditText edit_text = (EditText) dialogView.findViewById(R.id.dialog_edit_text);
        edit_text.setText(ImageNameUtil.getLastName(path));
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        String newName = edit_text.getText().toString();
                        String name = IMAGE_FILE_PATH+"/"+newName+ImageNameUtil.getTyle(path);

                        File oldFile = new File(path);

                        File newFile = new File(name);

                        Log.d(TAG, "onClick: newName = " + name);
                        oldFile.renameTo(newFile);
                        imageList.set(position,newFile);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,"已更改:"+
                                edit_text.getText().toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        customizeDialog.show();
    }



    private boolean readFromUDisk() {
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
                    return true;
                }
            }
        }
        return  false;
    }







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





    private void readTxtFromUDisk(UsbFile usbFile) {
        UsbFile descFile = usbFile;
        if(descFile.isDirectory()){
            Log.d(TAG, "readTxtFromUDisk: usbFile = "+usbFile.getName());
            try {
                UsbFile[] usbFiles = descFile.listFiles();

                    copyUSbFile(usbFiles);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制 USB 文件到本地
     *
     * @param file USB文件
     */
    private void copyUSbFile(final UsbFile[] file) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //复制结果
                int result = 0;
                try {
                    for (UsbFile usbFile : file){
                        //复制到本地的文件路径
                        String filePath = IMAGE_FILE_PATH + File.separator + usbFile.getName();
                        //开始写入
                        UsbFileInputStream uis = new UsbFileInputStream(usbFile);//读取选择的文件的

                        FileOutputStream fos = new FileOutputStream(filePath);

                        long avi = usbFile.getLength();
                        int writeCount = 0;
                        int bytesRead;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = uis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            writeCount += bytesRead;
                        }
                        fos.flush();
                        uis.close();
                        fos.close();

                        //传送文件复制的进度
                        Message msg = mHandler.obtainMessage();
                        msg.what = 101;
                        msg.arg1 = result+=1;
                        Log.d(TAG, "run: msg.arg1 = " +result);
                        msg.arg2 = file.length;
                        mHandler.sendMessage(msg);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {

                    OK = true;
                }
            }
        }).start();
    }

    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * @description OTG广播注册
     * @author
     * @time 2019/2/22 14:19
     */
    private void registerUDiskReceiver() {
        //监听otg插入 拔出
        receiver = new USBBroadCastReceiver();
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(receiver, filter);
        uDiskReceiverListener();
    }


    /**
     * Listener
     *
     * @yangtao
     *
     * @time 2019.2.22 16；34
     *
     */
    private void uDiskReceiverListener(){
        receiver.setUsbListener(new USBBroadCastReceiver.UsbListener(){

            //u盘Listener
            @Override
            public void insertUsb(UsbDevice device_add) {
                //接收到U盘插入广播，尝试读取U盘设备数据
                Log.d(TAG, "onReceive: 接受到U盘设备插入广播");
                showToastMsg("U盘已插入");
                redUDiskDevsList();

            }

            @Override
            public void removeUsb(UsbDevice device_remove) {
                showToastMsg("U盘已拔出");
            }

            @Override
            public void getReadUsbPermission(UsbDevice usbDevice) {
                readDevice(getUsbMass(usbDevice));
                Log.d(TAG, "onReceive: 接受自定义广播");
            }

            @Override
            public void failedReadUsb(UsbDevice usbDevice) {
                showToastMsg("未获取到U盘权限");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


}
