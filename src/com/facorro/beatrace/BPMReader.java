package com.facorro.beatrace;


public class BPMReader {
	
	private static final float NS2S = 1.0f / 1000000000.0f;
	private long start;
	private long end;
	private boolean dirty;
	private float lapse;

	public BPMReader() {
		this.start = 0;
		this.end = 0;
		this.dirty = true;
		this.lapse = 0;
	}

	public void tap() {
		long instant = System.nanoTime();

		if(this.start == 0) {
			this.start = instant;
		}
		else {
			this.start = this.end;
		}
	
		this.end = instant;
		/// Indicate that the lapse has to be recalculated
		this.dirty = true;
	}
	
	public float getLapse() {
		if(this.dirty)
		{
			this.lapse = (float)(this.end - this.start) * NS2S;
			this.dirty = false;
		}
	
		return this.lapse;
	}
	
	public float getBpm() {
		if(this.getLapse() > 0)
			return 60 / this.getLapse();
		else
			return 0;
	}	
}	
