package com.example.musicaction;



import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;
import android.widget.Toast;
import de.l3s.boilerpipe.document.TextBlock;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
//import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class TabMainActivity extends FragmentActivity implements TabListener, edu.cmu.pocketsphinx.RecognitionListener, android
.speech.RecognitionListener{
	
	private static final String COMMAND_SEARCH = "command";
	private static final String PLAY_COMMAND = "play";
	private static final String PLAY_SONG_COMMAND = "play song";
	private static final String PAUSE_COMMAND = "pause";
	private static final String NEXT_COMMAND = "next";
	private static final String PREV_COMMAND = "previous";
	private static final String ARTIST_COMMAND = "artist";
	private static final String ALBUM_COMMAND = "album";
	private static final String VOLUME_RAISE = "raise volume";
	private static final String VOLUME_LOWER = "lower volume";
	
	private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "music player";
    private static final String SONG_SEARCH = "songnames";
    
    private static boolean listening_to_name = false;
    private static String heard_name = "";
    
    private SpeechRecognizer recognizer;
    //private HashMap<String, Integer> captions;
	
    private List<TextBlock> textBlocks;
	
	
	private static final int ALLSONGS_TAB = 0;
	private static final int ALBUMS_TAB = 1;
	private static final int ARTISTS_TAB = 2;
	private static final int PLAYLISTS_TAB = 3;
	
	private ListView list_view;
	private ListView albums_view;
	private ArrayList<Song> allSongs = null;
	private ArrayList<Song> listSongs = null;
	private ArrayList<Album> allAlbums;
	private ArrayList<Artist> allArtists;
	private ArrayList<Song> nowPlaying = null;
	private ArrayList<String> allSongsTitle = null;
	private HashMap<String, Song> allSongsMap;
	private String allSongsTitle_str = "";
	private int currSong = 0;
	private MediaController mc;
	private MediaPlayer mp;
	private int mp_state = 0;
	private ImageButton prev, next, play, nowplayingbutt;
	private int back_flag;
	private int innerArtist = 0;
	private int innerAlbum = 0;
	private ActionBar actionBar;
	
	private android.speech.SpeechRecognizer googleSpeech = null;
	private Intent googleRecognizerIntent;

	@Override 
	public void onBackPressed()
	{
		if(back_flag != 0)
		{
			if(innerAlbum != 0)
			{
				innerAlbum = 0;
				back_flag = 0;
				showAllAlbums();
				return;
			}
			if(innerArtist != 0)
			{
				innerArtist = 0;
				back_flag = 0;
				showAllArtists();
				return;
			}
			
			
			super.onBackPressed();
		}
		return;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_main);
		
		back_flag = 0;
		actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
	    //initialise google recogniser
	    googleSpeech = android.speech.SpeechRecognizer.createSpeechRecognizer(this);
        googleSpeech.setRecognitionListener(this);
        googleRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        googleRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN");
        //googleRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        googleRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        googleRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
	    
	    //allSongs = getAllSongs();
	    //nowPlaying = allSongs;
	    prev = (ImageButton)findViewById(R.id.prevbutt);
	    next = (ImageButton)findViewById(R.id.nextbutt);
	    play = (ImageButton)findViewById(R.id.playbutt);
	    nowplayingbutt = (ImageButton)findViewById(R.id.nowplayingbutt);
	    // for each of the sections in the app, add a tab to the action bar.
	    Tab allSongsTab = actionBar.newTab();
	    //allSongs.setText("All Songs");
	   
	    allSongsTab.setTabListener(this);
	    allSongsTab.setIcon(R.drawable.music_new);
	    actionBar.addTab(allSongsTab);
	    
	    Tab albumsTab = actionBar.newTab();
	    //allSongs.setText("All Songs");
	   	    
	    albumsTab.setIcon(R.drawable.albums_new);
	    albumsTab.setTabListener(this);
	    actionBar.addTab(albumsTab);
	    
	    Tab artistsTab = actionBar.newTab();
	    //allSongs.setText("All Songs");
	   	    
	    artistsTab.setIcon(R.drawable.artists_new);
	    artistsTab.setTabListener(this);
	    actionBar.addTab(artistsTab);
	    
//	    Tab playlistsTab = actionBar.newTab();
//	    //allSongs.setText("All Songs");
//	   	    
//	    playlistsTab.setIcon(R.drawable.playlists_new);
//	    playlistsTab.setTabListener(this);
//	    actionBar.addTab(playlistsTab);
	    
	    mc = new MediaController(this);
	    
	    mp = new MediaPlayer();
	    mc.setAnchorView(nowplayingbutt);
	    mc.setMediaPlayer(new MediaPlayerControl() {
			
			@Override
			public void start() {
				mp.start();
				
			}
			
			@Override
			public void seekTo(int arg0) {
				mp.seekTo(arg0);
				
			}
			
			@Override
			public void pause() {
				mp.pause();
				
			}
			
			@Override
			public boolean isPlaying() {
				// TODO Auto-generated method stub
				return mp.isPlaying();
			}
			
			@Override
			public int getDuration() {
				return mp.getDuration();
				
			}
			
			@Override
			public int getCurrentPosition() {
				return mp.getCurrentPosition();
			}
			
			@Override
			public int getBufferPercentage() {
				return 0;
			}
			
			@Override
			public int getAudioSessionId() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean canSeekForward() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public boolean canSeekBackward() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public boolean canPause() {
				// TODO Auto-generated method stub
				return true;
			}
		});
	    OnClickListener prevlistener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playPreviousSong();	
			}
		};
	    prev.setOnClickListener(prevlistener);
	    
	    OnClickListener nextlistener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playNextSong();
			}
		};
	    next.setOnClickListener(nextlistener);
	    
	    OnClickListener playlistener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playPauseSong();
			}
		};
	    play.setOnClickListener(playlistener);
	    
	    new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(TabMainActivity.this);
                    
                    File assetDir = assets.syncAssets();
                    
                    setupRecognizer(assetDir);
                    
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
//                    ((TextView) findViewById(R.id.caption_text))
//                            .setText("Failed to init recognizer " + result);
                	Toast.makeText(getApplicationContext(), "Failed to initialise recogniser", Toast.LENGTH_LONG).show();
                } else {
                	Toast.makeText(getApplicationContext(), "Recognizer Initialised", Toast.LENGTH_LONG).show();
                	Log.e("Recognizer", "initialised");
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
	    
		
	}//end of oncreate......
	
	private void playPreviousSong()
	{
		if(nowPlaying == null)
		{
			if(allSongs == null)
			{
				allSongs = getAllSongs();
			}
			nowPlaying = allSongs;
		}
		Song s = nowPlaying.get(0);
		if(currSong != 0)
		{
			s = nowPlaying.get(--currSong);
		}
		
		
		if(mp != null)
		{
			mp.stop();
			mp.release();
			playSong(s);
		}
		else
		{
			playSong(s);
		}
	
	}
	private void playNextSong()
	{
		if(nowPlaying == null)
		{
			if(allSongs == null)
			{
				allSongs = getAllSongs();
			}
			nowPlaying = allSongs;
		}
		Song s = nowPlaying.get(0);
		if(currSong != (nowPlaying.size()-1))
		{
			s = nowPlaying.get(++currSong);
		}
		
		if(mp != null)
		{
			mp.stop();
			mp.release();
			playSong(s);
		}
		else
		{
			playSong(s);
		}
	}
	private void playPauseSong()
	{
		if(nowPlaying == null)
		{
			if(allSongs == null)
			{
				allSongs = getAllSongs();
			}
			nowPlaying = allSongs;
		}
		Song s = nowPlaying.get(currSong);
		if((mp != null) && mp.isPlaying())
		{
			mp.pause();
		}
		else{
			if(mp == null || mp_state == 0)
			{
				playSong(s);
				
			}
			
			else
			{
				mp.start();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tab_main, menu);
		return true;
	}

	public void doActionSearch(String value)
	{
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Search Dialog");
		SearchUtil su = new SearchUtil(allSongsTitle_str, allSongsTitle, allSongsMap);
		final ArrayList<Song> res = su.search(value);
		if(res == null)
		{
			Log.e("I was in null if", value);
			makeText(this, "Song not recognised", Toast.LENGTH_SHORT);
		}
		else if(res.size() == 1)
		{
			Song s = res.get(0);
			currSong = allSongs.indexOf(s);
			nowPlaying = allSongs;
			playSong(s);
		}
		else
		{
			Log.e("I was in else", value);
		  ListView listView = new ListView(this);
		  SongAdapter sa = new SongAdapter(this, res);
		  listView.setAdapter(sa);
		  alertDialogBuilder.setView(listView);
		  final AlertDialog al = alertDialogBuilder.create();
		  listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long rowId) {
				currSong = position;
				nowPlaying = res;
				Song s = nowPlaying.get(currSong);
				playSong(s);
				al.cancel();
				
			}
			  
		});
		  
		  al.show();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_search) {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Search Dialog");
			alertDialogBuilder.setMessage("Input text to search");
			final EditText input = new EditText(this);
			alertDialogBuilder.setView(input);
			final Context context = this;
			
			alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				  String value = input.getText().toString();
				  doActionSearch(value);
				}
			});

				alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});
				
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();


			return true;
		}
		else if(id == R.id.action_aboutus)
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("About Us");
			Context context = getApplicationContext();
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			final TextView mnames = new TextView(context); 
			mnames.setText("Developers : ");
			mnames.setTextColor(Color.parseColor("#000000"));
			final TextView bline1 = new TextView(context);
			bline1.setText("          ");
			final TextView names = new TextView(context);
			names.setText("Aaron Prince, Jaishankar Hebballi");
			names.setTextColor(Color.parseColor("#000000"));
			final TextView bline2 = new TextView(context);
			bline2.setText("          ");
			final TextView mdesc = new TextView(context);
			mdesc.setText("Project : ");
			final TextView bline3 = new TextView(context);
			bline3.setText("          ");
			mdesc.setTextColor(Color.parseColor("#000000"));
			final TextView desc = new TextView(context);
			desc.setText("Music Player with voice recognition");
			desc.setTextColor(Color.parseColor("#000000"));
			layout.addView(bline1);
			layout.addView(mnames);
		
			layout.addView(names);
			layout.addView(bline2);
			layout.addView(mdesc);
			
			layout.addView(desc);
			layout.addView(bline3);
			alert.setView(layout);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
			alert.show();
		}
		else if(id == R.id.action_instructions)
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Instructions : ");
			Context context = getApplicationContext();
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			final TextView mnames = new TextView(context); 
			mnames.setText("Initially");
			mnames.setTextColor(Color.parseColor("#000000"));
			final TextView bline1 = new TextView(context);
			bline1.setText("          ");
			final TextView names = new TextView(context);
			names.setText("Say <music player> for startup");
			names.setTextColor(Color.parseColor("#000000"));
			final TextView bline2 = new TextView(context);
			bline2.setText("          ");
			final TextView mdesc = new TextView(context);
			mdesc.setText("Next give any of the following instructions :");
			final TextView bline3 = new TextView(context);
			bline3.setText("          ");
			mdesc.setTextColor(Color.parseColor("#000000"));
			final TextView desc = new TextView(context);
			desc.setText("<play>,<pause>,<next>,<previous>,<artist>,<album>,<allsongs>,<play song : song name>,<raise/decrease volume>");
			desc.setTextColor(Color.parseColor("#000000"));
			layout.addView(bline1);
			layout.addView(mnames);
		
			layout.addView(names);
			layout.addView(bline2);
			layout.addView(mdesc);
			
			layout.addView(desc);
			layout.addView(bline3);
			alert.setView(layout);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
			alert.show();
		}
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
	private void playSong(Song s)
	{
		try {
			
			mp = new MediaPlayer();
			mp.setDataSource(s.getData());
			mp.prepare();
			mp.start();
			mc.setAnchorView(findViewById(R.id.mediacontrols));
		    mc.show();
		    mp_state = 1;
		    mp.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mpl) {
					
					playNextSong();
					
				}
			});
		    
		    Toast.makeText(getApplicationContext(),s.getTitle() + " is now playing", Toast.LENGTH_SHORT).show();
		    
			
			
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	
	}
	
	private void showAllSongs()
	{
		list_view = (ListView) findViewById(R.id.listView1);
	    allSongs = getAllSongs();
		SongAdapter adapter = new SongAdapter(getApplicationContext(),allSongs);
		list_view.setAdapter(adapter);
		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long rowId) {
				Song s = allSongs.get(position);
				Toast.makeText(getApplicationContext(),s.getTitle() + " is now playing", Toast.LENGTH_SHORT).show();
				currSong = position;
				nowPlaying = allSongs;
				if(mp != null)
				{
					mp.stop();
					mp.release();
				}
				playSong(s);
			}
			});
		
	}
	
	private void showAllAlbums()
	{
		list_view = (ListView) findViewById(R.id.listView1);
	    allAlbums = getAllAlbums();
		AlbumAdapter adapter = new AlbumAdapter(getApplicationContext(),allAlbums);
		list_view.setAdapter(adapter);
		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long rowId) {
				back_flag = 1;
				innerAlbum = 1;
				Album a = allAlbums.get(position);
				//Toast.makeText(getApplicationContext(),a.getTitle() + " is now playing", Toast.LENGTH_SHORT).show();
				list_view = (ListView) findViewById(R.id.listView1);
			    listSongs = a.getSongs();
				SongAdapter adapter = new SongAdapter(getApplicationContext(),listSongs);
				list_view.setAdapter(adapter);
				list_view.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view, int position,
							long rowId) {
						Song s = listSongs.get(position);
						
						Toast.makeText(getApplicationContext(),s.getTitle() + " is now playing", Toast.LENGTH_SHORT).show();
						currSong = position;
						if(mp != null)
						{
							mp.stop();
							mp.release();
						}
						nowPlaying = listSongs;
						playSong(s);
					}
					});
				
				
					
								
			}
			
		});
		
	}
	
	private void showAllArtists()
	{
		
		list_view = (ListView) findViewById(R.id.listView1);
	    allArtists = getAllArtists();
		ArtistAdapter adapter = new ArtistAdapter(getApplicationContext(),allArtists);
		list_view.setAdapter(adapter);
		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long rowId) {
				Artist a = allArtists.get(position);
				back_flag = 1;
				innerArtist = 1;
				//Toast.makeText(getApplicationContext(),a.getTitle() + " is now playing", Toast.LENGTH_SHORT).show();
				list_view = (ListView) findViewById(R.id.listView1);
			    listSongs = a.getSongs();
				SongAdapter adapter = new SongAdapter(getApplicationContext(),listSongs);
				list_view.setAdapter(adapter);
				list_view.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view, int position,
							long rowId) {
						Song s = listSongs.get(position);
						Toast.makeText(getApplicationContext(),s.getTitle() + " is now playing", Toast.LENGTH_SHORT).show();
						currSong = position;
						if(mp != null)
						{
							mp.stop();
							mp.release();
						}
						nowPlaying = listSongs;
						playSong(s);
					}
					});
				
				
					
								
			}
			
		});
		
	}
	
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		Fragment fragment = new DummySectionFragment();
	    Bundle args = new Bundle();
	    //args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
	        //tab.getPosition() + 1);
	    //fragment.setArguments(args);
	    //getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
	    
	    switch(tab.getPosition())
	    {
	    	case ALLSONGS_TAB: showAllSongs();
	    						break;
	    	case ALBUMS_TAB: showAllAlbums();
								break;
	    	case ARTISTS_TAB: showAllArtists();
								break;
	    	default: break;
	    	
	    }
	    
	}
	
	
	ArrayList<Song> getAllSongs()
	{
		if(this.allSongs == null)
		{
			allSongsTitle = new ArrayList<>();
			allSongsMap = new HashMap<>();
			ArrayList<Song> allSongs = new ArrayList<>();
			String projection[] = {MediaStore.Audio.AudioColumns.TITLE,
									MediaStore.Audio.AudioColumns.ALBUM,
									MediaStore.Audio.AudioColumns.ARTIST,
									MediaStore.Audio.AudioColumns.DURATION,
									MediaStore.Audio.AudioColumns.DATA};
			
			
			Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
			int albumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM);
			int titleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE);
			int artIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST);
			int durIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION);
			int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA);
			while(cursor.moveToNext())
			{
				Song song = new Song();
				String title = cursor.getString(titleIdx);
				song.setTitle(title);
				song.setAlbum(cursor.getString(albumIdx));
				song.setArtist(cursor.getString(artIdx));
				song.setData(cursor.getString(dataIdx));
				allSongs.add(song);
				allSongsTitle.add(title);
				if(allSongsMap.get(title) == null)
				{
					allSongsMap.put(title, song);
				}
				allSongsTitle_str = allSongsTitle_str + " " + title;
				
			}
			cursor.close();
			Collections.sort(allSongs);
			return allSongs;
		}
		else
		{
			return this.allSongs;
		}
		
	}
	
	ArrayList<Album> getAllAlbums()
	{
		if(this.allAlbums == null)
		{
			if(this.allSongs == null)
			{
				this.allSongs = getAllSongs();
				
			}

			HashMap<String, Album> albums = new HashMap<>();
			for(Song s : allSongs)
			{
				String al_title = s.getAlbum();
				Album a = albums.get(al_title);
				if(a == null)
				{
					ArrayList<Song> as = new ArrayList<>();
					as.add(s);
					Album ab = new Album(al_title, as);
					albums.put(al_title, ab);
				}
				else
				{
					a.getSongs().add(s);
				}
			}
			this.allAlbums = new ArrayList<>(albums.values());
			Collections.sort(allAlbums);
			return this.allAlbums;
		}
		else
		{
			return this.allAlbums;
		}
		
	}
	ArrayList<Artist> getAllArtists()
	{
		if(this.allArtists == null)
		{
			if(this.allSongs == null)
			{
				this.allSongs = getAllSongs();
				
			}

			HashMap<String, Artist> artists = new HashMap<>();
			for(Song s : allSongs)
			{
				String al_title = s.getArtist();
				Artist a = artists.get(al_title);
				if(a == null)
				{
					ArrayList<Song> as = new ArrayList<>();
					as.add(s);
					Artist at = new Artist(al_title, as);
					artists.put(al_title, at);
				}
				else
				{
					a.getSongs().add(s);
				}
			}
			this.allArtists = new ArrayList<>(artists.values());
			Collections.sort(allArtists);
			return this.allArtists;
		}
		else
		{
			return this.allArtists;
		}
		
	}
	
	

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
		
	}
	
	/** * A dummy fragment representing a section of the app */

	  public static class DummySectionFragment extends Fragment {
	    public static final String ARG_SECTION_NUMBER = "placeholder_text";

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	      TextView textView = new TextView(getActivity());
	      textView.setGravity(Gravity.CENTER);
	      textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
	      return textView;
	    }
	  }
	  
	  
	  //implementation of recognition listener
	  
	  @Override
	    public void onPartialResult(Hypothesis hypothesis) {
	        String text = hypothesis.getHypstr();
	        if (text.equals(KEYPHRASE))
	            switchSearch(COMMAND_SEARCH);
//	        else if (text.equals(PLAY_COMMAND))
//	        {
//	        	listening_to_name = true;
//	            switchSearch(SONG_SEARCH);
//	        }
//	        else if (text.equals(SONG_SEARCH))
//	            switchSearch(SONG_SEARCH);
//	        else
//	            //((TextView) findViewById(R.id.result_text)).setText(text);
//	        	Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
	    }

	    @Override
	    public void onResult(Hypothesis hypothesis) {
	        //((TextView) findViewById(R.id.result_text)).setText("");
	        if (hypothesis != null) {
	            String text = hypothesis.getHypstr();
//	            if(listening_to_name)
//	            {
//	            	if(!text.equals(PLAY_COMMAND))
//	            	{
//		            	Log.e("full text", text);
//		            	doActionSearch(text);
//		            	makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
//		            	listening_to_name = false;
//	            	}
//	            }
	            if(text.equals(PLAY_SONG_COMMAND))
	            {
	            	recognizer.stop();
	            	googleSpeech.startListening(googleRecognizerIntent);
	            	//googleSpeech.stopListening();
	            	
	            	//playPauseSong();
	            }
	            else if(text.equals(PLAY_COMMAND))
	            {
	            	playPauseSong();
	            }
	            else if(text.equals(PAUSE_COMMAND))
	            {
	            	playPauseSong();
	            }
	            else if(text.equals(NEXT_COMMAND))
	            {
	            	playNextSong();
	            }
	            else if(text.equals(PREV_COMMAND))
	            {
	            	playPreviousSong();
	            }
	            else if(text.equals(ARTIST_COMMAND))
	            {
	            	Tab t = actionBar.getTabAt(ARTISTS_TAB);
	            	actionBar.selectTab(t);
	            }
	            else if(text.equals(ALBUM_COMMAND))
	            {
	            	Tab t = actionBar.getTabAt(ALBUMS_TAB);
	            	actionBar.selectTab(t);
	            }
	            else if(text.equals(VOLUME_RAISE))
	            {
	            	AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
	            	audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
	            }
	            else if(text.equals(VOLUME_LOWER))
	            {
	            	AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
	            	audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
	            }
	            else if(text.equals(COMMAND_SEARCH))
	            {
	            	//st
	            }
	            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	        }
	    }

	    @Override
	    public void onBeginningOfSpeech() {
	    }

	    @Override
	    public void onEndOfSpeech() {
	        
	            switchSearch(KWS_SEARCH);
	    }

	    private void switchSearch(String searchName) {
	        recognizer.stop();
	        recognizer.startListening(searchName);
	        //String caption = getResources().getString(captions.get(searchName));
	        //((TextView) findViewById(R.id.caption_text)).setText(caption);
	        //Toast.makeText(getApplicationContext(), "Give Commands...", Toast.LENGTH_SHORT);
	    }

	    private void setupRecognizer(File assetsDir) {
	        File modelsDir = new File(assetsDir, "models");
	        recognizer = defaultSetup()
	                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
	                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
	                .setRawLogDir(assetsDir).setKeywordThreshold(1e-40f)
	                .getRecognizer();
	        recognizer.addListener(this);
	        

	        // Create keyword-activation search.
	        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
	        // Create grammar-based searches.
	        
	        File commandGrammar = new File(modelsDir, "grammar/commands.gram");
	        recognizer.addGrammarSearch(COMMAND_SEARCH, commandGrammar);
	        File songGrammar = new File(modelsDir, "grammar/songnames.gram");
	        recognizer.addGrammarSearch(SONG_SEARCH, songGrammar);
//	        File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
//	        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
//	        // Create language model search.
//	        File languageModel = new File(modelsDir, "lm/cmusphinx-5.0-en-us.lm.dmp");
//	        recognizer.addNgramSearch(SONG_SEARCH, languageModel);
	    }

//implementing android.speech.RecognitionListener;
	    
		@Override
		public void onBufferReceived(byte[] arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onError(int arg0) {
			// TODO Auto-generated method stub
			Log.e("Error",Integer.toString(arg0));
			googleSpeech.cancel();
			recognizer.startListening(KWS_SEARCH);
			//googleSpeech.startListening(googleRecognizerIntent);
			
		}


		@Override
		public void onEvent(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onPartialResults(Bundle arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onReadyForSpeech(Bundle arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onResults(Bundle results) {
			// TODO Auto-generated method stub
			ArrayList <String> matches = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
			String text = matches.get(0);
			
			doActionSearch(text);
			makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			googleSpeech.stopListening();
			recognizer.startListening(KWS_SEARCH);
			
		}


		@Override
		public void onRmsChanged(float arg0) {
			// TODO Auto-generated method stub
			
		}

}
