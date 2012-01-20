package com.facorro.beatrace.fmod;

public class Sound {
	
	private static int counter = 0; 
	
	private int id;
	private String filename;
	private System sys;
	
	public Sound(String filename)
	{
		this.id = Sound.counter++;		
		this.filename = filename;
	}
	
	public void open()
	{
		cOpen(filename);
	}
	
	public void play()
	{
		cPlay();
	}
	
	public void pause()
	{
		cPause();
	}
	
	public float getFrequency() {
		return cGetFrequency();
	}
	
	public void setFrequency(float rate)
	{
		cSetFrequency(rate);
	}
	
	public float getBpm()
	{
		return cGetBpm();
	}
	
	public int getRead() 
	{
		return cGetRead();
	}
	
	public int getLenght()
	{
		return cGetLength();
	}
	
	public int getLengthInMilis()
	{
		return cGetLengthInMilis();
	}
	
	public int getPosition()
	{
		return cGetPosition();
	}
	
	private void close()
	{
		cClose();
	}
	
	public static int getEnoughSamples()
	{
		return cGetEnoughSamples();
	}
	
	public static int getProcessedSamples()
	{
		return cGetProcessedSamples();
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.close();
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

	public native void cOpen(String filename);
	public native void cClose();
	public native void cPlay();
	public native void cPause();
	public native void cSetFrequency(float freq);
	public native float cGetFrequency();
	public native float cGetBpm();
	public native int cGetRead();
	public native int cGetLength();
	public native int cGetLengthInMilis();
	public native int cGetPosition();
	public static native int cGetEnoughSamples();
	public static native int cGetProcessedSamples();
	
}
