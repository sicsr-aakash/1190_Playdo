package com.example.musicaction;

public class Song implements Comparable {
	private String title;
	private String album;
	private String artist;
	private int duration;
	private String data;
	
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	@Override
	public int compareTo(Object s) throws ClassCastException {
		if(!(s instanceof Song))
			throw new ClassCastException("A Song object expected.");
		return title.compareToIgnoreCase(((Song) s).getTitle());
	}
	
}	

