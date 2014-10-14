/**
 * 
 */
package com.example.musicaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
/**
 * @author jaishankar
 *
 */
public class SearchUtil {
	private String allSongs_str;
	private ArrayList<String> allSongsTitle;
	private HashMap<String, Song> allSongs;
	private HashMap<String, ArrayList<String>> keywords;
	private List<TextBlock> textBlocks;
	private TextDocument td;
	
	public SearchUtil(String allSongs_str, ArrayList<String> allSongsTitle,
			HashMap<String, Song> allSongs) {
		
		this.allSongs_str = allSongs_str;
		this.allSongsTitle = allSongsTitle;
		this.allSongs = allSongs;
		keywords = new HashMap<>();
		textBlocks = new ArrayList<>();
		init();
		//td = new TextDocument(textBlocks);
		
//		ArrayList<String> a = keywords.get("War");
//		Log.e("title_str", a.get(0));
	}
	private void init()
	{
		for(String s: allSongsTitle)
		{
			StringTokenizer st = new StringTokenizer(s);
			while(st.hasMoreElements())
			{
				String token = st.nextToken().toLowerCase();
				ArrayList<String> a;
				if((a = keywords.get(token)) == null)
				{
					a = new ArrayList<>();
					a.add(s);
					keywords.put(token, a);
			//		textBlocks.add(new TextBlock(token));
				}
				else
				{
					a.add(s);
				}
			}
		}
	}
	public ArrayList<Song> search(String searchString)
	{
		ArrayList<String> searchkeys = new ArrayList<>();
		ArrayList<Song> ret_arr = new ArrayList<>();
		HashSet<String> hs = new HashSet<>();
		HashSet<String> res_hs;// = new HashSet<>();
		ArrayList<String> titles;// = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(searchString);
		while(st.hasMoreTokens())
		{
			searchkeys.add(st.nextToken().toLowerCase());
		}
		titles = keywords.get(searchkeys.get(0));
		if(titles == null)
			return null;
		if(searchkeys.size() == 1)
		{
			titles = keywords.get(searchkeys.get(0));
			if(titles == null)
				return null;
			for(String s : titles)
			{
				ret_arr.add(allSongs.get(s));
			}
			return ret_arr;
		}
		res_hs = new HashSet(keywords.get(searchkeys.get(0)));
		for(String s : searchkeys)
		{
			hs = new HashSet(keywords.get(s));
			res_hs.retainAll(hs);
		}
		titles = new ArrayList<>(res_hs);
		boolean exists = false;
		for(String s : titles)
		{
			Song song = allSongs.get(s);
			if(song !=null)
			{
				ret_arr.add(song);
				exists = true;
			}
		}
		if(exists)
			return ret_arr;
		return null;
		
	}
	public List<String> getAllKeywords()
	{
		return new ArrayList<String>(keywords.keySet());
	}
	
	
	
	
	
	
}
