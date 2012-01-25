package com.facorro.beatrace.utils;

import java.util.List;

public interface BeatDetection {
	void addValue(float value);
	void registerListener(BeatListener beatListener);
	void notifyListeners();
	List<Float> getValues();
	float getMin();
	float getMax();
	float getMedium();
}
