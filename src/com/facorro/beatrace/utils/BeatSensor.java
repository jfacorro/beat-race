package com.facorro.beatrace.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class BeatSensor implements SensorEventListener {
	private final float GRAVITY_INVERSE = 1 / 9.8f;
	
	private BeatDetection beatDetection;

	private float[] gravity;
	
	private SensorManager sensorManager;
	private Sensor gravitySensor;
	private Sensor linearAccelerationSensor;

	public BeatSensor(BeatDetection beatDetection, SensorManager sensorManager) {
		this.beatDetection = beatDetection;
		
        // Find needed sensors to detect beat while running
        this.sensorManager = sensorManager;        
        this.gravitySensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.linearAccelerationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	}
	
	public void register()	{
		this.sensorManager.registerListener(this, this.gravitySensor, 75000);
		this.sensorManager.registerListener(this, this.linearAccelerationSensor, 75000);
	}
	
	public void unregister() {
		this.sensorManager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
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

			this.beatDetection.addValue(value);
		}			
	}
}
