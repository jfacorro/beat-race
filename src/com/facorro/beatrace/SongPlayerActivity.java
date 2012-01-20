package com.facorro.beatrace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.facorro.beatrace.fmod.Sound;
import com.facorro.beatrace.fmod.System;
import com.facorro.beatrace.utils.BPMReader;
import com.facorro.beatrace.utils.BeatListener;

public class SongPlayerActivity extends Activity implements SensorEventListener, BeatListener {
	
	public enum SongPlayerState {
		INITIALIZING,
		LOADING_SONG,
		CALCULATING_BPM,
		PLAYING_SONG,
		FINISHED_SONG,
		STOPPING
	}
	
	private final float GRAVITY_INVERSE = 1 / 9.8f;
	
	ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
	/**
	 * Sensors related API objects
	 */
	private SensorManager sensorManager;
	private Sensor gravitySensor;
	private Sensor linearAccelerationSensor;
	
	private String filename;
	
	/**
	 * Object that contains both values and the drawing logic
	 */
	private SongView songView;
	
	private float originalFrequency;
	
	private List<Float> log = new LinkedList<Float>(); 
	
	/**
	 * Bpm Reader used to calculate the bpm based on taps
	 */
	private BPMReader bpmReader = new BPMReader();

	private float[] gravity;
	private float songBpm;
	private float userBpm;
	private System fmodSystem = new com.facorro.beatrace.fmod.System();
	private Sound sound;
	
	private SongPlayerState state = SongPlayerState.INITIALIZING;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_player);
        
        // Get filename from intent data
        this.filename = this.getIntent().getStringExtra("filename");
        this.filename = this.filename.replace("/mnt/", "/");
        // Get song view instance from layout
        this.songView = (SongView)this.findViewById(R.id.songView);
        this.songView.getThread().setBeatListener(this);
        
        // Find needed sensors to detect beat while running
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);        
        this.gravitySensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.linearAccelerationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        
        this.state = SongPlayerState.LOADING_SONG;
        // Initialize FMOD system, load song and get BPM
        this.fmodSystem.init();
    	this.sound = new Sound(this.filename);
    	this.sound.open();
		this.originalFrequency = this.sound.getFrequency();
		
		this.calculateBpm();
	}
	
    @Override
    public void onStop()
    {
    	this.state = SongPlayerState.STOPPING;

    	this.fmodSystem.stop();
    	
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

    	super.onStop();
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();
    	
    	this.registerSensorListeners();      
    }
    
    private void registerSensorListeners()
    {
    	if(this.state == SongPlayerState.PLAYING_SONG)
        {
    		this.sensorManager.registerListener(this, this.gravitySensor, 75000);
    		this.sensorManager.registerListener(this, this.linearAccelerationSensor, 75000);
        }
    }

    @Override
    protected void onPause() {
		super.onPause();

		this.sensorManager.unregisterListener(this);
    }
	
	/**
	 * Slows down the song
	 * @param view
	 */
	public void slower(View view)
	{
		this.sound.setFrequency(this.sound.getFrequency() - 100);
	}
	
	/**
	 * Speeds up the song
	 * @param view
	 */
	public void faster(View view)
	{
		this.sound.setFrequency(this.sound.getFrequency() + 100);
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
		new Thread(){
			@Override
			public void run()
			{
				toneGenerator.startTone(ToneGenerator.TONE_DTMF_0);
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				toneGenerator.stopTone();
			}
		}.start();
	}
	
    public void calculateBpm()
    {
    	new Thread() {
    		public void run() {
    			state = SongPlayerState.CALCULATING_BPM;
    	    	songBpm = sound.getBpm();
    			songView.getThread().setSongBpm(songBpm);
    			sound.play();
    			state = SongPlayerState.PLAYING_SONG;
    			registerSensorListeners();
    		}
    	}.start();
    	
    	// Create progress dialog.
    	final ProgressDialog mProgressDialog = new ProgressDialog(SongPlayerActivity.this);    	
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
		mProgressDialog.show();
    	
    	new Thread() {
    		public void run() {
    			int total = Sound.getEnoughSamples();
    			int read = 0;
    			int completed = 0;

    			while(completed < 100 && mProgressDialog.isShowing())
    			{
    				try 
    				{
	    				completed = read * 100 / total;
	    				mProgressDialog.setProgress(completed);	
	    				read = Sound.getProcessedSamples();

						sleep(500);
					} 
    				catch (InterruptedException e) 
    				{
						e.printStackTrace();
					}
    			}

    			mProgressDialog.dismiss();
    		};
    	}.start();
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_GRAVITY)
		{
			this.gravity = event.values.clone();
		}
		else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			// Use gravity values to find out projection in the
			// z axis.
			float x = event.values[0] * this.gravity[0];
			float y = event.values[1] * this.gravity[1];
			float z = event.values[2] * this.gravity[2];
			float value = (x + y + z) * GRAVITY_INVERSE;

			this.songView.getThread().addValue(value);
		}			
	}
}
