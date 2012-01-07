package com.facorro.beatrace;

import org.fmod.FMODAudioDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SongPlayerActivity extends Activity {
	
	private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
	private String filename;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_player);
        
        Intent intent = this.getIntent();
        
        TextView text = (TextView) this.findViewById(R.id.txtTest);
        
        //SurfaceView view = (SurfaceView) this.findViewById(R.id.surfaceView);
        
        this.filename = intent.getStringExtra("filename");
        this.filename = this.filename.replace("/mnt/", "/");
        text.setText(this.filename);
	}
	
    @Override
    public void onStart()
    {
    	super.onStart();
    	
    	mFMODAudioDevice.start();
    	
    	cBegin(this.filename);
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
    
	public native void cBegin(String filename);
	public native void cUpdate();
	public native void cEnd();
	public native void cPause();
	public native boolean cGetPaused();
}
