package com.example.musicaction;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArtistAdapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<Artist> artists;
	
	public ArtistAdapter(Context context, List artists) {
		super(context, R.layout.oneartist, artists);
		this.context = context;
		this.artists = (ArrayList<Artist>)artists;
		// TODO Auto-generated constructor stub
	}
	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.oneartist, null);
		final TextView title = (TextView) rowView.findViewById(R.id.oneartistname);
		//textView.setTextColor("#000000");
		title.setText(artists.get(position).getTitle());
		return rowView;
	}

}
