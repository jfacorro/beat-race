package com.facorro.beatrace.utils;

import java.util.LinkedList;
import java.util.List;


public class BeatCounter implements BeatListener {
	private static final float NS2S = 1.0f / 1000000000.0f;
	private static final int HISTORY_VALUES = 20;
	private static final int SECONDS_IN_MINUTES = HISTORY_VALUES * 60;
	private long start;
	private long end;
	private boolean dirty;
	private List<Float> lapseValues;
	private float lapse;

	public BeatCounter() {
		this.start = 0;
		this.end = 0;
		this.dirty = true;
		this.lapseValues = new LinkedList<Float>();
		this.lapse = 0;
	}

	public void tap() {
		long instant = System.nanoTime();

		if(this.start == 0)
			this.start = instant;
		else
			this.start = this.end;
	
		this.end = instant;
		/// Indicate that the lapse has to be recalculated
		this.dirty = true;
	}
	
	public float getLapse() {
		if(this.dirty)
		{
			if(this.lapseValues.size() >= HISTORY_VALUES)
			{
				this.lapse -= this.lapseValues.get(0);
				this.lapseValues.remove(0);
			}
			
			float lastValue = (float)(this.end - this.start) * NS2S;

			this.lapseValues.add(lastValue);
			
			this.lapse += lastValue;
			
			this.dirty = false;
		}
	
		return this.lapse;
	}
	
	public float getBpm() {
		float lapse = this.getLapse();

		if(lapse > 0)
			return SECONDS_IN_MINUTES / lapse;
		else
			return 0;
	}

	public void beat() {
		this.tap();		
	}	
}	
