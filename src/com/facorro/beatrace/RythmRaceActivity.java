package com.facorro.beatrace;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RythmRaceActivity extends Activity implements TextWatcher
{    
	private SimpleCursorAdapter songsAdapter;
	private ListView lstSongs;
	private EditText txtSearch;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        
        // EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
        this.txtSearch = (EditText) findViewById(R.id.txtSearch);
        this.lstSongs = (ListView)findViewById(R.id.lstSongs);
        		
        this.refreshSongList();
        
        // Add on click event listener for search button
        this.txtSearch.addTextChangedListener(this);

        lstSongs.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				loadSong();
			}        	
		});
    }
    
    public void loadSong()
    {
    	Cursor cursor = this.songsAdapter.getCursor();
    	
    	int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
    	
    	String filename = cursor.getString(dataIndex);
    	
    	Intent playSongIntent = new Intent(RythmRaceActivity.this, SongPlayerActivity.class);
    	
    	playSongIntent.putExtra("filename", filename);
    	
    	RythmRaceActivity.this.startActivity(playSongIntent); 
    }
    
    /**
     * Queries audio media database for all existing songs 
     * @param filter
     * @return Filtered list of songs
     */
    public void updateSongsAdapter(String filter)
    {
    	String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
        // If the filter is not null or empty then use its value
        // to filter by artist and title
    	if (filter != null && filter.trim() != "")
    	{
		    selection += "AND (" + MediaStore.Audio.Media.ARTIST + " LIKE '%" + filter + "%'"; 
		    selection += "OR " + MediaStore.Audio.Media.TITLE + " LIKE '%" + filter + "%')";
    	}
        		
        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID
        };

        Cursor cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);
        
        this.songsAdapter = new SimpleCursorAdapter(
        		this,
        		R.layout.song_item,
        		cursor,
        		new String[]{
        				MediaStore.Audio.Media._ID,
	        			MediaStore.Audio.Media.ARTIST,
	                    MediaStore.Audio.Media.TITLE,
        		},
        		new int[] {
                        R.id.lblArtist,
                        R.id.lblArtist,
                        R.id.lblTitle});
    }
    
    private void refreshSongList()
    {
    	this.updateSongsAdapter(this.txtSearch.getText().toString());
    	this.lstSongs.setAdapter(this.songsAdapter);
    }
    
    /**
     * TextWatcher Implementation
     */
	
    public void afterTextChanged(Editable s) {
    	// Start filtering once there are at least 3 characters.
		if(s.length() >= 3 || s.length() == 0)
		{
			this.refreshSongList();
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}
}