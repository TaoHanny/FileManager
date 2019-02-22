package com.shutuo.filemanagement.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shutuo.filemanagement.R;

import java.io.File;
import java.util.List;

public class FileListAdapter extends BaseAdapter {

    private List<File> list;

    private Context context;

    public FileListAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
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

        holder.icon.setImageURI(Uri.fromFile(file));
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
