package com.facorro.beatrace;

import org.fmod.FMODAudioDevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SongPlayerActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	
	private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
	private String filename;
	private TextView txtBpm;
	private SongView songView;
	
	private float originalFrequency;
	private float currentFrequency;
	
	private BPMReader bpmReader = new BPMReader();
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_player);
        
        Intent intent = this.getIntent();
        this.filename = intent.getStringExtra("filename");
        this.filename = this.filename.replace("/mnt/", "/");
        
        this.txtBpm = (TextView) this.findViewById(R.id.txtBpm);
        this.songView = (SongView)this.findViewById(R.id.songView);
        
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);        
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == 0)
		{
			this.bpmReader.tap();
			this.updateBpm();
		}
		return true;
	}
	
	public void slower(View view)
	{
		this.currentFrequency -= 100;
		cSetFrequency(this.currentFrequency);
	}
	
	public void faster(View view)
	{
		this.currentFrequency += 100;
		cSetFrequency(this.currentFrequency);
	}
	
	private void updateBpm()
	{
		float bpm = cGetBpmSoFar();    
		String message = "BPM: " + Float.toString(bpm);
		message += "\n Sample Rate: " + Float.toString(this.currentFrequency);
		message += "\n Tap BPM: " + Float.toString(this.bpmReader.getBpm());
    	this.txtBpm.setText(message);
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
    	
    	super.onStop();
    }
    
    @Override
    protected void onResume() {
      super.onResume();
      this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
      super.onPause();
      this.sensorManager.unregisterListener(this);
    }
	
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

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub		
	}

	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == this.accelerometer.getType())
		{
			if(event.values.length > 2)
			{
				this.songView.setAcceleration(event.values[2]);
			}
		}
	}
}
