package com.example.pillmasterapp;

import static com.example.pillmasterapp.login.sId;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListViewAdapterResult extends BaseAdapter {

    private ArrayList<ListViewItem> listViewItemList = new ArrayList<>();

    public ListViewAdapterResult() { }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (sId == null) {
                convertView = inflater.inflate(R.layout.result_item_before_login, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.result_item, parent, false);
            }
        }

        ImageView pillImageView = convertView.findViewById(R.id.pill_img);
        TextView pillNameTextView = convertView.findViewById(R.id.pill_name);

        ListViewItem listViewItem = listViewItemList.get(position);

        pillNameTextView.setText(listViewItem.getPill_name());

        // Firebase imageUrl 있으면 Glide로 로딩
        if (listViewItem.getImageUrl() != null && !listViewItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(listViewItem.getImageUrl())
                    .placeholder(R.drawable.main_logo)  // 프로젝트에 있는 기본 이미지
                    .error(R.drawable.main_logo)
                    .into(pillImageView);
        }

        return convertView;
    }

    public String getPillName(int position) {
        return listViewItemList.get(position).getPill_name();
    }

    // URL 기반 추가
    public void addItem(String imageUrl, String name) {
        ListViewItem item = new ListViewItem();
        item.setImageUrl(imageUrl);
        item.setPill_name(name);
        listViewItemList.add(item);
    }
}



