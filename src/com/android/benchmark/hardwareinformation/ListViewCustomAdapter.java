package com.android.benchmark.hardwareinformation;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.benchmark.R;

public class ListViewCustomAdapter extends BaseAdapter {
	public ArrayList<String> title;
	public ArrayList<String> description;
	public ArrayList<Integer> images;

	public Activity context;
	public LayoutInflater inflater;

	public ListViewCustomAdapter(Activity context) {
		super();
		this.context = context;

		title = new ArrayList<String>();
		description = new ArrayList<String>();
		images = new ArrayList<Integer>();

		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void add(String title, String description, int image) {
		this.title.add(title);
		this.description.add(description);
		this.images.add(image);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return title.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static class ViewHolder {
		ImageView imgViewLogo;
		TextView txtViewTitle;
		TextView txtViewDescription;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.listitem_row, null);

			holder.imgViewLogo = (ImageView) convertView
					.findViewById(R.id.imgViewLogo);
			holder.txtViewTitle = (TextView) convertView
					.findViewById(R.id.txtViewTitle);
			holder.txtViewDescription = (TextView) convertView
					.findViewById(R.id.txtViewDescription);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.imgViewLogo.setImageResource(images.get(position));
		holder.txtViewTitle.setText(title.get(position));
		holder.txtViewDescription.setText(description.get(position));

		return convertView;
	}

}