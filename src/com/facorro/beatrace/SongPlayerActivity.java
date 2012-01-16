package com.facorro.beatrace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.fmod.FMODAudioDevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.facorro.beatrace.utils.BeatListener;

public class SongPlayerActivity extends Activity implements SensorEventListener, BeatListener {
	private final float GRAVITY_INVERSE = 1 / 9.8f;
	/**
	 * Sensors related API objects
	 */
	private SensorManager sensorManager;
	private Sensor gravitySensor;
	private Sensor linearAccelerationSensor;
	
	private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
	private String filename;
	
	private BufferedWriter logFile;

	/**
	 * Object that contains both values and the drawing logic
	 */
	private SongView songView;
	
	private float originalFrequency;
	private float currentFrequency;
	
	private List<Float> log = new LinkedList<Float>(); 
	
	/**
	 * Bpm Reader used to calculate the bpm based on taps
	 */
	private BPMReader bpmReader = new BPMReader();

	private float[] gravity;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_player);
        
        Intent intent = this.getIntent();
        this.filename = intent.getStringExtra("filename");
        this.filename = this.filename.replace("/mnt/", "/");
        
        this.songView = (SongView)this.findViewById(R.id.songView);
        
        this.songView.getThread().setBeatListener(this);
        
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);        
        this.gravitySensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.linearAccelerationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        
		try {
			File path = Environment.getExternalStorageDirectory();
			File songfile = new File(this.filename);
			File destination = new File(path, songfile.getName().replace(" ", "") + ".csv");
			
			this.logFile = new BufferedWriter(new FileWriter(destination));
		} catch (IOException e) {
			Log.d("Error creating log file", e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Slows down the song
	 * @param view
	 */
	public void slower(View view)
	{
		this.currentFrequency -= 100;
		cSetFrequency(this.currentFrequency);
	}
	
	/**
	 * Speeds up the song
	 * @param view
	 */
	public void faster(View view)
	{
		this.currentFrequency += 100;
		cSetFrequency(this.currentFrequency);
	}
	
	private void updateBpm()
	{
		float songBpm = cGetBpmSoFar();
		float userBpm = this.bpmReader.getBpm();

		this.songView.getThread().setSongBpm(songBpm);
		this.songView.getThread().setUserBpm(userBpm);
		
    	log.add(songBpm);
    	log.add(userBpm);
    	log.add(userBpm / songBpm);
	}
	
	public void beat()
	{
		this.bpmReader.tap();
		this.updateBpm();
		this.playTone();
	}
	
	public void playTone()
	{
		new Thread(){
			public void run()
			{
				ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
				toneGenerator.startTone(ToneGenerator.TONE_DTMF_0);
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				toneGenerator.stopTone();
			}
		}.start();
	}
	
    @Override
    public void onStart()
    {
    	super.onStart();
    	
    	mFMODAudioDevice.start();
    	
    	this.originalFrequency = cBegin(this.filename);
    	this.currentFrequency = this.originalFrequency;
    }
    
    @Override
    public void onStop()
    {
    	cEnd();
    	mFMODAudioDevice.stop();
    	
		try {
			for(int i = 0; i < this.log.size(); i += 3)
			{
				this.logFile.write(Integer.toString(i / 3)+ ";");
				this.logFile.write(Float.toString(this.log.get(i)).replace(".", ",")+ ";");
				this.logFile.write(Float.toString(this.log.get(i + 1)).replace(".", ",")+ ";");
				this.logFile.write(Float.toString(this.log.get(i + 2)).replace(".", ",") + "\n");
			}
			this.logFile.close();
			Log.d("Wrote file", " and closed it.");
		} catch (IOException e) {
			e.printStackTrace();
		}

    	super.onStop();
    }
    
    @Override
    protected void onResume() {
      super.onResume();
      this.sensorManager.registerListener(this, this.gravitySensor, 75000);
      this.sensorManager.registerListener(this, this.linearAccelerationSensor, 75000);
    }

    @Override
    protected void onPause() {
      super.onPause();
      this.sensorManager.unregisterListener(this);
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

    /**
     * External JNI methods
     */
	static
    {
    	System.loadLibrary("fmodex");
        System.loadLibrary("main");
    }
    
	public native float cBegin(String filename);
	public native void cUpdate();
	public native void cEnd();
	public native void cPause();
	public native boolean cGetPaused();
	public native void cSetFrequency(float freq);
	public native float cGetBpm();
	public native float cGetBpmSoFar();
	public native int cGetLengthInMilis();
	public native int cGetPosition();
	
}
