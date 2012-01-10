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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SongPlayerActivity extends Activity implements SensorEventListener {
	private static final float TAP_THRESHOLD_VALUE = 6.0f;

	/**
	 * Sensors related API objects
	 */
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor rotationSensor;
	
	private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
	private String filename;
	private TextView txtBpm;

	/**
	 * Object that contains both values and the drawing logic
	 */
	private SongView songView;
	
	private float originalFrequency;
	private float currentFrequency;
	
	private float [] rotationValues = new float[4];
	private final float[] rotationMatrix = new float[16];
	
	/**
	 * Bpm Reader used to calculate the bpm based on taps
	 */
	private BPMReader bpmReader = new BPMReader();

	/**
	 * Used for tapping detection
	 */
	private boolean tap = false;
	
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
        this.accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.rotationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        
        rotationMatrix[ 0] = 1;
        rotationMatrix[ 4] = 1;
        rotationMatrix[ 8] = 1;
        rotationMatrix[12] = 1;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == 0)
		{
			this.bpmReader.tap();
		}
		return true;
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
		float bpm = cGetBpmSoFar();
		float userBpm = this.bpmReader.getBpm();
		String message = "BPM: " + Float.toString(bpm);
		message += "\n Sample Rate: " + Float.toString(this.currentFrequency);
		message += "\n Tap BPM: " + Float.toString(userBpm);
		message += " - Ratio: " + Float.toString(userBpm / bpm);
    	this.txtBpm.setText(message);
	}
	
	private void detectUserBpm(float value)
	{
		if(!Float.isInfinite(value))
		{
			this.songView.addValue(value);
			
			if (this.songView.beat())
			{
				this.bpmReader.tap();
				this.updateBpm();
			}
		}
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
      this.sensorManager.registerListener(this, this.accelerometerSensor, 60000);
      this.sensorManager.registerListener(this, this.rotationSensor, 60000);
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
		if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			float[] source = new float[4];
			source[0] = event.values[0];
			source[1] = event.values[1];
			source[2] = event.values[2];
			source[3] = 0;
			float[] result = new float[4];
			android.opengl.Matrix.multiplyMV(result, 0, this.rotationMatrix, 0, source, 0);
			//this.songView.addValue(result[2]);
			this.detectUserBpm(result[2]);
		}
		else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
		{
            SensorManager.getRotationMatrixFromVector(this.rotationMatrix, event.values);
		}
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
}
