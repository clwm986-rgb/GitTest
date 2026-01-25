package com.example.pillmasterapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapterDetail extends BaseAdapter {

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<>();

    // 생성자
    public ListViewAdapterDetail() { }

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
            convertView = inflater.inflate(R.layout.show_detail_item, parent, false);
        }

        TextView pillNameTextView = convertView.findViewById(R.id.detail_pill_name);
        TextView companyTextView = convertView.findViewById(R.id.company);
        TextView ingredientTextView = convertView.findViewById(R.id.ingredient);
        TextView volumnTextView = convertView.findViewById(R.id.volumn);

        ListViewItem listViewItem = listViewItemList.get(position);

        pillNameTextView.setText(listViewItem.getPill_name());
        companyTextView.setText(listViewItem.getCompany());
        ingredientTextView.setText(listViewItem.getIngredient());
        volumnTextView.setText(listViewItem.getVolumn());

        return convertView;
    }

    public String getPillName(int position) {
        return listViewItemList.get(position).getPill_name();
    }

    public String getPillComp(int position) {
        return listViewItemList.get(position).getCompany();
    }

    // 아이템 데이터 추가 함수
    public void addItem(String pillname, String company, String ingredient, String volumn) {
        ListViewItem item = new ListViewItem();
        item.setPill_name(pillname);
        item.setCompany(company);
        item.setIngredient(ingredient);
        item.setVolumn(volumn);
        listViewItemList.add(item);
    }
}

