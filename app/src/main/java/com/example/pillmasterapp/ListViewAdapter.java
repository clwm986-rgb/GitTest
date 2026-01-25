package com.example.pillmasterapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<>();

    // ListViewAdapter의 생성자
    public ListViewAdapter() { }

    // Adapter에 사용되는 데이터의 개수를 리턴
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    // 지정한 위치(position)에 있는 데이터 리턴
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴
    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getPillName(int position) {
        return listViewItemList.get(position).getPill_name();
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        ImageView pillImageView = convertView.findViewById(R.id.imageView1);
        TextView pillNameTextView = convertView.findViewById(R.id.name);
        TextView nicknameTextView = convertView.findViewById(R.id.name_user);

        ListViewItem listViewItem = listViewItemList.get(position);

        pillNameTextView.setText(listViewItem.getPill_name());
        nicknameTextView.setText(listViewItem.getNickname());

        // ✅ Firebase 이미지 URL을 Glide로 로딩
        Glide.with(context)
                .load(listViewItem.getImageUrl())
                .placeholder(R.drawable.default_pill) // drawable 폴더에 기본 이미지 필요
                .into(pillImageView);

        return convertView;
    }

    // 아이템 데이터 추가 함수
    public void addItem(String pillName, String nickname, String imageUrl) {
        ListViewItem item = new ListViewItem();
        item.setPill_name(pillName);
        item.setNickname(nickname);
        item.setImageUrl(imageUrl);
        listViewItemList.add(item);
    }
}
