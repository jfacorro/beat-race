package com.facorro.beatrace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import com.facorro.beatrace.SongPlaybackService.ServiceBinder;
import com.facorro.beatrace.SongView.SongThread;
import com.facorro.beatrace.fmod.Sound;
import com.facorro.beatrace.utils.BeatListener;

public class SongPlayerActivity extends Activity implements BeatListener {

	// Object that contains both values and the drawing logic
	private SongView songView;
	
	// Initial song filename
	private String initalFilename;
	
	// Logged values
	private List<Float> log = new LinkedList<Float>(); 
	
	// Playback and sensor service
	private SongPlaybackService playbackService;
	
	// Defines callbacks for service binding, passed to bindService()
    private ServiceConnection connection = new ServiceConnection() {

    	public void onServiceDisconnected(ComponentName name) {
			
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			ServiceBinder binder = (ServiceBinder) service;
			playbackService = binder.getService();
			playbackService.setActivity(SongPlayerActivity.this);
			openSong(initalFilename);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view for the activity
        setContentView(R.layout.song_player);
        // Get song view instance from layout
        this.songView = (SongView)this.findViewById(R.id.songView);
        //this.songView.getThread().setBeatListener(this);
        this.initalFilename = this.getIntent().getStringExtra("filename").replace("/mnt/", "/");
        // Start the service
        Intent intent = new Intent(SongPlayerActivity.this, SongPlaybackService.class);
        this.startService(intent);
	}
	
	protected void openSong(String filename) {
		this.playbackService.open(filename);
	}

	@Override
	protected void onStart() {
		super.onStart();
        Intent intent = new Intent(SongPlayerActivity.this, SongPlaybackService.class);
        this.bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}
	
    @Override
    public void onStop() {
    	super.onStop();
    	this.unbindService(connection);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	// Stop service
    	Intent intent = new Intent(this, SongPlaybackService.class);
        this.stopService(intent);
        
        this.writeLog();   	
    }
	
	private void writeLog() {
		// Create log file for song    	
		try {
			File path = Environment.getExternalStorageDirectory();
			File songfile = new File(this.initalFilename);
			File destination = new File(path, songfile.getName().replace(" ", "") + ".csv");
			BufferedWriter logFile = new BufferedWriter(new FileWriter(destination));

			for(int i = 0; i < this.log.size(); i += 3) {
				logFile.write(Integer.toString(i / 3)+ ";");
				logFile.write(Float.toString(this.log.get(i)).replace(".", ",")+ ";");
				logFile.write(Float.toString(this.log.get(i + 1)).replace(".", ",")+ ";");
				logFile.write(Float.toString(this.log.get(i + 2)).replace(".", ",") + "\n");
			}

			logFile.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void beat() {
		float userBpm = this.playbackService.getUserBpm();
		float songBpm = this.playbackService.getSongBpm();
		this.songView.getThread().setSongBpm(songBpm);
		this.songView.getThread().setUserBpm(userBpm);
    	log.add(songBpm);
    	log.add(userBpm);
    	log.add(userBpm / songBpm);
	}

	public void showProgressDialog() {
    	// Create progress dialog.
    	final ProgressDialog progressDialog = new ProgressDialog(this);
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
		
		Thread updateProgressThread = new Thread(new Runnable() {
			public void run() {
    			int total = Sound.getEnoughSamples();
    			int read = 0;
    			int completed = 0;
    			
    			while(completed < 100 && progressDialog.isShowing()) {
    				try {
	    				completed = read * 100 / total;
	    				progressDialog.setProgress(completed);	
	    				read = Sound.getProcessedSamples();

						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    			progressDialog.dismiss();
			}
		});
		
		progressDialog.show();		
		updateProgressThread.start();		
	}

	public SongThread getSongView() {
		return this.songView.getThread();
	}
}
