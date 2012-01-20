package com.facorro.beatrace.fmod;

import org.fmod.FMODAudioDevice;

public class System {
	
	private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
	
	public void init() {
		mFMODAudioDevice.start();
		cInit();
	}
	
	public void update() {
		cUpdate();
	}
	
	public void stop() {
		mFMODAudioDevice.stop();
		cStop();
	}
	
	@Override
	protected void finalize() throws Throwable {
		mFMODAudioDevice.stop();
		cStop();
		super.finalize();
	}
	
    /**
     * External JNI methods
     */
	static
    {
    	java.lang.System.loadLibrary("fmodex");
    	java.lang.System.loadLibrary("main");
    }

	public native void cInit();
	public native void cUpdate();
	public native void cStop();
}
