package com.shutuo.filemanager.adapter;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shutuo.filemanagement.R;
import com.shutuo.filemanager.util.LruCacheUtils;


import java.io.File;
import java.util.List;

public class FileListAdapter extends BaseAdapter {

    private List<File> list;

    private Context context;


    //sdcard指定存放图片文件绝对路径
    private final static String IMAGE_FILE_PATH = "/sdcard/arcFace/register";

    public FileListAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
//        LruCacheUtils.init();
    }

    @Override
    public int getCount() {

        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.file_main_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        File file = list.get(position);
        LruCacheUtils.getImage(file.getPath());
        Glide.with(context).load(file).override(100,100).fitCenter().placeholder(R.mipmap.loading).into(holder.icon);

        holder.name.setText(file.getName());
        return convertView;
    }

    private class ViewHolder {

        private TextView name;

        private ImageView icon;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.file_item_text);

            icon = (ImageView) view.findViewById(R.id.file_item_image);
        }
    }
}
