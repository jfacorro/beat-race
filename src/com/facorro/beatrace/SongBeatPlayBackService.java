package com.facorro.beatrace;

import com.facorro.beatrace.fmod.Sound;
import com.facorro.beatrace.fmod.System;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SongBeatPlayBackService extends Service {
	
	public enum SongPlayerState {
		INITIALIZING,
		LOADING_SONG,
		CALCULATING_BPM,
		PLAYING_SONG,
		FINISHED_SONG,
		STOPPING
	}

	private System fmodSystem = new com.facorro.beatrace.fmod.System();
	private Sound sound;

	private String filename;
	
	@Override
	public void onCreate() {
		this.fmodSystem.init();

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        this.filename = intent.getStringExtra("filename");
        this.filename = this.filename.replace("/mnt/", "/");
        
        if(this.sound != null)
        {
        	this.sound.close();
        }
        
		this.sound = new Sound(this.filename);
    	this.sound.open();

		return 0;
	}
	
	@Override
	public void onDestroy() {
		this.fmodSystem.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
