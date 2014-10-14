package com.example.musicaction;

import java.util.ArrayList;

public class Artist implements Comparable {
	private String title;
	private ArrayList<Song> songs;
	
	public String getTitle() {
		return this.title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ArrayList<Song> getSongs() {
		return songs;
	}
	public void setSongs(ArrayList<Song> songs) {
		this.songs = songs;
	}
	
	public Artist(String title, ArrayList<Song> songs) {
		super();
		this.title = title;
		this.songs = songs;
	}
	@Override
	public int compareTo(Object a) throws ClassCastException {
		if(!(a instanceof Artist))
			throw new ClassCastException("A Song object expected.");
		return title.compareToIgnoreCase(((Artist) a).getTitle());
	}
	

}
