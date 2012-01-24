package com.facorro.beatrace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.facorro.beatrace.SongBeatPlayBackService.ServiceBinder;
import com.facorro.beatrace.utils.BeatCounter;

public class SongPlayerActivity extends Activity {
	
	public enum SongPlayerState {
		INITIALIZING,
		LOADING_SONG,
		CALCULATING_BPM,
		PLAYING_SONG,
		FINISHED_SONG,
		STOPPING
	}
	
	ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);

	private String filename;
	
	/**
	 * Object that contains both values and the drawing logic
	 */
	private SongView songView;
	
	private List<Float> log = new LinkedList<Float>(); 
	
	/**
	 * Bpm Reader used to calculate the bpm based on taps
	 */
	private BeatCounter bpmReader = new BeatCounter();
	
	private boolean serviceBound;
	private SongBeatPlayBackService playbackService;
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			serviceBound = false;			
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			 // We've bound to LocalService, cast the IBinder and get LocalService instance
			Toast.makeText(SongPlayerActivity.this, "onServiceConnected...", Toast.LENGTH_SHORT).show();
			ServiceBinder binder = (ServiceBinder) service;
			playbackService = binder.getService();
            serviceBound = true;
            playSong();
		}
	};

	private float songBpm;
	private float userBpm;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "Create event...", Toast.LENGTH_SHORT).show();
        
        setContentView(R.layout.song_player);
        
        // Get song view instance from layout
        this.songView = (SongView)this.findViewById(R.id.songView);
        //this.songView.getThread().setBeatListener(this);
        this.filename = this.getIntent().getStringExtra("filename").replace("/mnt/", "/");

        Intent intent = new Intent(this, SongBeatPlayBackService.class);
        this.startService(intent);
	}
	
	protected void playSong() {
		this.playbackService.play(this.filename);		
	}

	@Override
	protected void onStart() {
		super.onStart();
        Intent intent = new Intent(this, SongBeatPlayBackService.class);
        this.bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}
	
    @Override
    protected void onResume() 
    {
    	super.onResume();    	
    	Toast.makeText(this, "Resume event...", Toast.LENGTH_SHORT).show(); 
    }    

    @Override
    protected void onPause()
    {
		super.onPause();		
		Toast.makeText(this, "Pause event...", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	Toast.makeText(this, "Stop event...", Toast.LENGTH_SHORT).show();
    	this.unbindService(connection);
    }
    
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
    	Toast.makeText(this, "Destroy event...", Toast.LENGTH_SHORT).show();
    	
    	Intent intent = new Intent(SongPlayerActivity.this, SongBeatPlayBackService.class);
        this.stopService(intent);
    	
		// Create log file for song    	
		try 
		{
			File path = Environment.getExternalStorageDirectory();
			File songfile = new File(this.filename);
			File destination = new File(path, songfile.getName().replace(" ", "") + ".csv");
			BufferedWriter logFile = new BufferedWriter(new FileWriter(destination));

			for(int i = 0; i < this.log.size(); i += 3)
			{
				logFile.write(Integer.toString(i / 3)+ ";");
				logFile.write(Float.toString(this.log.get(i)).replace(".", ",")+ ";");
				logFile.write(Float.toString(this.log.get(i + 1)).replace(".", ",")+ ";");
				logFile.write(Float.toString(this.log.get(i + 2)).replace(".", ",") + "\n");
			}

			logFile.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
    }
	
	private void updateBpm()
	{
		this.userBpm = this.bpmReader.getBpm();
		this.songView.getThread().setUserBpm(this.userBpm);
		
    	log.add(this.songBpm);
    	log.add(this.userBpm);
    	log.add(this.userBpm / this.songBpm);
	}
	
	public void beat()
	{
		this.bpmReader.tap();
		this.playTone();
		this.updateBpm();
	}
	
	public void playTone()
	{
		Thread toneThread = new Thread(new Runnable() {
			public void run() {
				toneGenerator.startTone(ToneGenerator.TONE_DTMF_0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				toneGenerator.stopTone();
			}
		});
		
		toneThread.start();
	}
}
