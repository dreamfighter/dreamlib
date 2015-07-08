package com.dreamfighter.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.dreamfighter.android.webadapter.entity.BaseEntity;

public abstract class ListViewAdapter extends BaseAdapter{
	private List<BaseEntity> itemDetailsrrayList;

	public ListViewAdapter(Context context, List<BaseEntity> results) {
		itemDetailsrrayList = results;
	}

	public int getCount() {
		return itemDetailsrrayList.size();
	}

	public Object getItem(int position) {
		return itemDetailsrrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public abstract View getView(int position, View convertView, ViewGroup parent);

}
