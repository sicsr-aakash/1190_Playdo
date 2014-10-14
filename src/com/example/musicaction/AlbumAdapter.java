package com.example.musicaction;

import java.util.ArrayList;
import java.util.List;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AlbumAdapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<Album> albums;
	
	public AlbumAdapter(Context context, List albums) {
		super(context, R.layout.onealbum, albums);
		this.context = context;
		this.albums = (ArrayList<Album>)albums;
		// TODO Auto-generated constructor stub
	}
	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.onealbum, null);
		final TextView title = (TextView) rowView.findViewById(R.id.onealbumname);
		//textView.setTextColor("#000000");
		title.setText(albums.get(position).getTitle());
		return rowView;
	}

}
