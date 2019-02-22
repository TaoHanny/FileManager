package com.shutuo.filemanagement;

import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.shutuo.filemanagement.adapter.FileListAdapter;
import com.shutuo.filemanagement.usbHelper.USBBroadCastReceiver;
import com.shutuo.filemanagement.usbHelper.UsbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class UsbActivity extends AppCompatActivity implements USBBroadCastReceiver.UsbListener {

    private ListView listView;
    private Button button;

    //本地文件列表相关
    private ArrayList<File> localList;
    private FileListAdapter<File> localAdapter;
    private String localRootPath = "/sdcard/arcFace/register/";
    private String localCurrentPath = "";
    //USB文件列表相关
    private ArrayList<UsbFile> usbList = new ArrayList<>();
    private FileListAdapter<UsbFile> usbAdapter;
    private UsbHelper usbHelper;

    private final static String U_DISK_FILE_NAME = "image";

    private final static String TAG = UsbActivity.class.getName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_activity_usb);
        listView = findViewById(R.id.file_usb_listview);
        button = findViewById(R.id.file_usb_button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFileName()){
                    showToastMsg("zhengzai     copy");
                    initLocalFile();
                }else {
                    showToastMsg("wenjian      bucunzai");
                }
            }
        });

    }


    private boolean isFileName(){
        if (null != usbList && usbList.size() > 0) {
            for (UsbFile usbFile : usbList) {
                if (usbFile.getName().equals(U_DISK_FILE_NAME)) {
                    Log.d(TAG, "isFileName "+usbFile.getName());
                    openUsbFile(usbFile);
                    return true;
                }
            }
        }
        return false;
    }





    /**
     * 初始化本地文件列表
     */
    private void initLocalFile() {
        localList = new ArrayList<>();
        Collections.addAll(localList, new File(localRootPath).listFiles());
        localCurrentPath = localRootPath;
        localAdapter = new FileListAdapter<>(this, localList);
        listView.setAdapter(localAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openLocalFile(localList.get(position));
            }
        });
    }


    /**
     * 打开本地 File
     *
     * @param file File
     */
    private void openLocalFile(File file) {
        if (file.isDirectory()) {
            //文件夹更新列表
            localList.clear();
            Collections.addAll(localList, file.listFiles());
//            localAdapter.notifyDataSetChanged();
            localCurrentPath = file.getAbsolutePath();
            Log.d(TAG, "openLocalFile: ");
        } else {
            //开启线程，将文件复制到本地
//            copyLocalFile(file);
        }
    }

    /**
     * 打开 USB File
     *
     * @param file USB File
     */
    private void openUsbFile(UsbFile file) {
        if (file.isDirectory()) {
            //文件夹更新列表
            ArrayList<UsbFile> usbFiles = usbHelper.getUsbFolderFileList(file);
            if(usbFiles.size()>0 && usbFiles!=null){

                for (UsbFile usbfile : usbFiles) {

                    copyUSbFile(usbfile);
                }

            }else {
                Toast.makeText(UsbActivity.this,"文件为空",Toast.LENGTH_LONG).show();
            }
//            usbList.clear();
//            usbList.addAll();
//            usbAdapter.notifyDataSetChanged();
        } else {
            //开启线程，将文件复制到本地
        }
    }


    /**
     * 更新 USB 文件列表
     */
    private void updateUsbFile(int position) {
        UsbMassStorageDevice[] usbMassStorageDevices = usbHelper.getDeviceList();
        if (usbMassStorageDevices.length > 0) {
            //存在USB
            usbList.clear();
            usbList = usbHelper.readDevice(usbMassStorageDevices[position]);
        } else {
            Log.e("UsbTestActivity", "No Usb Device");
            usbList.clear();
        }
    }


    /**
     * 复制 USB 文件到本地
     *
     * @param file USB文件
     */
    private void copyUSbFile(final UsbFile file) {
        //复制到本地的文件路径
        final String filePath = localCurrentPath + File.separator + file.getName();
        Log.d(TAG, "copyUSbFile: filePath = "+ filePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //复制结果
                final boolean result = usbHelper.saveUSbFileToLocal(file, filePath, new UsbHelper.DownloadProgressListener() {
                    @Override
                    public void downloadProgress(final int progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String text = "From Usb " + usbHelper.getCurrentFolder().getName()
                                        + "\nTo Local " + localCurrentPath
                                        + "\n Progress : " + progress;
//                                showProgressTv.setText(text);
                            }
                        });
                    }
                });
                //主线程更新UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            openLocalFile(new File(localCurrentPath));
                        } else {
                            Toast.makeText(UsbActivity.this, "复制失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }


    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void insertUsb(UsbDevice device_add) {
        showToastMsg("device   is   insert");
        if (usbList.size() == 0) {
            updateUsbFile(0);
        }
    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        showToastMsg("device  is   removeUsb");
        updateUsbFile(0);
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {

    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {

    }
}
