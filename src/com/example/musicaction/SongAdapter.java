package com.example.musicaction;

import java.util.ArrayList;
import java.util.List;





import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SongAdapter extends ArrayAdapter {
	private final Context context;
	private ArrayList<Song> songs;

	public SongAdapter(Context context,List songs) {
		super(context, R.layout.onesong,songs);
		this.context = context;
		this.songs = (ArrayList<Song>)songs;
	}
	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.onesong, null);
		final TextView title = (TextView) rowView.findViewById(R.id.onesongname);
		//textView.setTextColor("#000000");
		title.setText(songs.get(position).getTitle());
		final TextView artistView = (TextView) rowView.findViewById(R.id.artist);
		//textView.setTextColor("#000000");
		artistView.setText(songs.get(position).getArtist());
		return rowView;
	}


}
