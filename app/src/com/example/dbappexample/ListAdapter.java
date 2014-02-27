package com.example.dbappexample;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter used for the list elements
 * 
 * @author jliebana
 * 
 */
public class ListAdapter extends BaseAdapter {

	private final Context context;
	private final ArrayList<EbookEntry> items;

	public ListAdapter(Context context, ArrayList<EbookEntry> items) {
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
			EbookEntry ebookEntry = items.get(position);
			icon.setPath(ebookEntry.getLocalPath());
			TextView itemLabel = (TextView) gridView.findViewById(R.id.list_item_label);
			itemLabel.setText(ebookEntry.getTitle());
			TextView itemDate = (TextView) gridView.findViewById(R.id.list_item_date);
			itemDate.setText(ebookEntry.getDate().toLocaleString());
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
