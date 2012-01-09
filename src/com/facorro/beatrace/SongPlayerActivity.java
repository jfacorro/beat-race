package com.facorro.beatrace;

import org.fmod.FMODAudioDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SongPlayerActivity extends Activity {
	
	private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
	private String filename;
	private TextView txtBpm;
	
	private float originalFrequency;
	private float currentFrequency;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_player);
        
        Intent intent = this.getIntent();
        this.filename = intent.getStringExtra("filename");
        this.filename = this.filename.replace("/mnt/", "/");
        
        this.txtBpm = (TextView) this.findViewById(R.id.txtBpm);
	}
	
	public void slower(View view)
	{
		this.currentFrequency -= 100;
		cSetFrequency(this.currentFrequency);

		this.updateBpm();
	}
	
	public void faster(View view)
	{
		this.currentFrequency += 100;
		cSetFrequency(this.currentFrequency);

		this.updateBpm();
	}
	
	private void updateBpm()
	{
		float bpm = cGetBpmSoFar();    	
    	this.txtBpm.setText(Float.toString(bpm));
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
