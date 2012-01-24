package com.facorro.beatrace;

import java.io.FileDescriptor;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;

import com.facorro.beatrace.fmod.Sound;
import com.facorro.beatrace.fmod.System;
import com.facorro.beatrace.utils.BeatCounter;
import com.facorro.beatrace.utils.BeatDetection;
import com.facorro.beatrace.utils.BeatListener;
import com.facorro.beatrace.utils.BeatSensor;
import com.facorro.beatrace.utils.CycleBeatDetection;

public class SongBeatPlayBackService extends Service {
	
	public enum SongPlayerState {
		INITIALIZING,
		LOADING_SONG,
		CALCULATING_BPM,
		PLAYING_SONG,
		FINISHED_SONG,
		STOPPING
	}

	private SongPlayerState state = SongPlayerState.INITIALIZING;
	private BeatListener beatListener;
	private BeatDetection beatDetection;
	private BeatSensor sensor;

	private System fmodSystem;
	private Sound sound;

	private String filename;
	
	private float songBpm;
	private float userBpm;
	
	private Binder binder = new ServiceBinder();
	
	@Override
	public void onCreate() {
		this.fmodSystem = new com.facorro.beatrace.fmod.System();
		this.fmodSystem.init();

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);		
		this.beatListener = new BeatCounter();
		this.beatDetection = new CycleBeatDetection(beatListener);
		this.sensor = new BeatSensor(beatDetection, sensorManager);		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Service onStartCommand...", Toast.LENGTH_SHORT).show();
		return Service.START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service onDestroy...", Toast.LENGTH_SHORT).show();

		this.fmodSystem.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "Service onBind...", Toast.LENGTH_SHORT).show();
		return this.binder;
	}
	
	public void play(String filename) {
		this.filename = filename;

        if(this.sound != null)
        {
        	this.sound.close();
        }
        
		this.sound = new Sound(this.filename);
    	this.sound.open();
    	
    	this.sound.play();
	}
	
	public void stop() {
		this.sound.stop();	
	}
	
    public void calculateBpm() {

    	Thread calcBpmThread = new Thread(new Runnable() {
			public void run() {
    			state = SongPlayerState.CALCULATING_BPM;
    	    	songBpm = sound.getBpm();
    			sound.play();
    			state = SongPlayerState.PLAYING_SONG;
			}
		});
    	
    	// Create progress dialog.
    	final ProgressDialog mProgressDialog = new ProgressDialog(SongBeatPlayBackService.this);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
		
		Thread updateProgressThread = new Thread(new Runnable() {
			public void run() {
    			int total = Sound.getEnoughSamples();
    			int read = 0;
    			int completed = 0;

    			while(completed < 100 && mProgressDialog.isShowing()) {
    				try {
	    				completed = read * 100 / total;
	    				mProgressDialog.setProgress(completed);	
	    				read = Sound.getProcessedSamples();

						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    			mProgressDialog.dismiss();				
			}
		});

    	calcBpmThread.setPriority(7);
		calcBpmThread.start();
		updateProgressThread.start();
		mProgressDialog.show();
    }
    
    public class ServiceBinder extends Binder {
    	
    	public SongBeatPlayBackService getService() {
			return SongBeatPlayBackService.this;			
		}
    }
}
