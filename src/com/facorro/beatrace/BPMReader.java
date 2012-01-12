package com.facorro.beatrace;

import java.util.LinkedList;
import java.util.List;


public class BPMReader {
	
	private static final float NS2S = 1.0f / 1000000000.0f;
	private static final float MAXVALUES = 5;
	private long start;
	private long end;
	private boolean dirty;
	private List<Float> lapseValues;
	private float lapse;

	public BPMReader() {
		this.start = 0;
		this.end = 0;
		this.dirty = true;
		this.lapseValues = new LinkedList<Float>();
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
			if(this.lapseValues.size() >= MAXVALUES)
			{
				this.lapseValues.remove(0);
			}

			this.lapseValues.add((float)(this.end - this.start) * NS2S);
			
			this.lapse = 0;
			
			for(float value : this.lapseValues)
				this.lapse += value;
			
			this.lapse /= this.lapseValues.size();

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
