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
	
	public void stop() {
		
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
	
	public void close()
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

	private native void cOpen(String filename);
	private native void cClose();
	private native void cPlay();
	private native void cPause();
	private native void cSetFrequency(float freq);
	private native float cGetFrequency();
	private native float cGetBpm();
	private native int cGetRead();
	private native int cGetLength();
	private native int cGetLengthInMilis();
	private native int cGetPosition();
	private static native int cGetEnoughSamples();
	private static native int cGetProcessedSamples();
}
