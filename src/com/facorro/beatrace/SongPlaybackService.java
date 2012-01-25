package com.facorro.beatrace;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import com.facorro.beatrace.data.DataAccess;
import com.facorro.beatrace.fmod.Sound;
import com.facorro.beatrace.fmod.System;
import com.facorro.beatrace.utils.BeatCounter;
import com.facorro.beatrace.utils.BeatDetection;
import com.facorro.beatrace.utils.BeatSensor;
import com.facorro.beatrace.utils.CycleBeatDetection;

public class SongPlaybackService extends Service {
	
    public class ServiceBinder extends Binder {    	
    	public SongPlaybackService getService() {
			return SongPlaybackService.this;
		}
    }
	
    // Expects to have its beat method called when a beat is detected
    private BeatCounter beatCounter;
    // Handles the beat detection algorithm and lets the listener know
    // when a beat is detected
	private BeatDetection beatDetection;
	// Handles the sensors signals and feeds the detection object 
	private BeatSensor sensor;

	// Sound system object.
	private System fmodSystem;
	private Sound sound;

	// Sound filename
	private String filename;
	
	// User and song BPMs
	private float songBpm;
	
	// The activity to which this service is bound
	private SongPlayerActivity activity;
	
	// Service's binder
	private ServiceBinder binder = new ServiceBinder();
	
	// Database access interface
	private DataAccess dataAccess = new DataAccess(this);
	
	@Override
	public void onCreate() {
		this.fmodSystem = new com.facorro.beatrace.fmod.System();
		this.fmodSystem.init();

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);		
		this.beatCounter = new BeatCounter();
		this.beatDetection = new CycleBeatDetection(beatCounter);
		this.sensor = new BeatSensor(beatDetection, sensorManager);
		this.dataAccess.open();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		stop();
		this.sound.close();
		this.fmodSystem.stop();
		this.dataAccess.close();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return this.binder;
	}
	
	/**
	 * Opens a new song given the filename
	 * @param filename
	 */
	public void open(String filename) {
		this.filename = filename;

        if(this.sound != null) {
        	this.sound.close();
        	this.sound = null;
        }
        
		this.sound = new Sound(this.filename);
    	this.sound.open();
    	this.resolveSongBpm();
	}
	
	/**
	 * Starts playing the song and enables the beat sensor.
	 */
	public void play() {
        sensor.startListening();
		sound.play();
	}
	
	/**
	 * Stops the song and disables beat sensor.
	 */
	public void stop() {
		this.sound.stop();
        this.sensor.stopListening();
	}
	
	/**
	 * Stops the song
	 */
	public void pause() {
		this.sound.pause();
        this.sensor.stopListening();
	}
	
	private void resolveSongBpm() {
		float bpm = dataAccess.getSongBpm(this.filename);

		if(bpm > 0)	{
			this.songBpm = bpm;
			play();
		} else {
			calculateSongBpm();
		}
	}
	
    private void calculateSongBpm() {

    	Thread calcBpmThread = new Thread(new Runnable() {
			public void run() {
    	    	songBpm = sound.getBpm();
    	    	dataAccess.saveSongBpm(filename, songBpm);
    			play();
			}
		});
    	
    	// Create progress dialog.
    	final ProgressDialog progressDialog = new ProgressDialog(this.activity);
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

    	calcBpmThread.setPriority(7);
		calcBpmThread.start();
		progressDialog.show();
		updateProgressThread.start();
		
    }
    
	public float getUserBpm() {
		return this.beatCounter.getBpm();
	}

	public float getSongBpm() {
		return this.songBpm;
	}
	
	public void setActivity(SongPlayerActivity activity) {
		this.activity = activity;
	}
}
