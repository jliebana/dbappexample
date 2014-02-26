package com.example.dbappexample;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI.Entry;

/**
 * Adapter used for the list elements
 * 
 * @author jliebana
 * 
 */
public class ListAdapter extends BaseAdapter {

	private final Context context;
	private final ArrayList<Entry> items;

	public ListAdapter(Context context, ArrayList<Entry> items) {
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View gridView;

		if (convertView == null) {
			gridView = new View(context);
			gridView = inflater.inflate(R.layout.item_layout, null);

			MyClickableImageView icon = (MyClickableImageView) gridView.findViewById(R.id.list_item_image);
			icon.setId(position);
			TextView itemLabel = (TextView) gridView.findViewById(R.id.list_item_label);
			itemLabel.setText(items.get(position).fileName());
			TextView itemDate = (TextView) gridView.findViewById(R.id.list_item_date);
			itemDate.setText(items.get(position).clientMtime);
		}
		else {
			gridView = convertView;
		}

		return gridView;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).hashCode();
	}
}
