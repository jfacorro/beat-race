package com.facorro.beatrace.utils;

import java.util.LinkedList;
import java.util.List;

public class CycleBeatDetection implements BeatDetection {
	private static final float TAP_THRESHOLD_VALUE = 2.0f;
	private static final int MAX_VALUES = 20;
	private static final float INIT_MIN_VALUE = -2.0f;
	private static final float INIT_MAX_VALUE = 2.0f;

	private List<Float> values;
	
	private float minValue = INIT_MIN_VALUE;
	private float maxValue = INIT_MAX_VALUE;
	private float mediumValue = (minValue + maxValue) / 2;

	private float lastSlope;
	private double cycle;
	
	private BeatListener beatListener;
	
	public CycleBeatDetection() {
		this.values = new LinkedList<Float>();
	}
	
	public void addValue(float value) {
		if(value < this.minValue)
		{
			this.minValue = value;
			this.updateMediumValue();
		}
		else if(value > this.maxValue)
		{
			this.maxValue = value;
			this.updateMediumValue();
		}

		if(this.values.size() >= MAX_VALUES)
		{
			this.values.remove(0);
		}

		this.values.add(value);
		
		this.detectCycle(value);
	}
	
	public void setBeatListener(BeatListener beatListener) {
		this.beatListener = beatListener;
	}
	
	private void updateMediumValue() {
		this.mediumValue = (this.maxValue + this.minValue) / 2;
	}
	
	private void detectCycle(double value) {
		float currentSlope = this.slope();
		if(this.lastSlope != currentSlope)
		{
			if(
				(this.lastSlope < 0 && value > this.mediumValue  + TAP_THRESHOLD_VALUE) ||
				(this.lastSlope > 0 && value < this.mediumValue + -TAP_THRESHOLD_VALUE)
			)
			{
				this.lastSlope = currentSlope;
				this.cycle += 1;
			}
			else if (this.lastSlope == 0)
			{
				this.lastSlope = currentSlope;
			}
		}
		
		if(this.cycle == 2)
		{
			this.beatListener.beat();
			this.cycle = 0;
		}
	}
	
	private float slope() {
		int count = this.values.size();
		float slope = 0;
		
		if(count >= 2)
		{
			slope = (float)(this.values.get(count - 1) - this.values.get(count - 2));
		}
		
		return slope;
	}
}
