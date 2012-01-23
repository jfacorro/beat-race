package com.facorro.beatrace;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.facorro.beatrace.utils.BeatDetection;
import com.facorro.beatrace.utils.BeatListener;

public class SongView extends SurfaceView implements SurfaceHolder.Callback {
	class SongThread extends Thread implements BeatDetection {
		private static final float TAP_THRESHOLD_VALUE = 2.0f;
		private static final int MAX_VALUES = 20;
		private static final float INIT_MIN_VALUE = -2.0f;
		private static final float INIT_MAX_VALUE = 2.0f;
		private static final float FONT_SIZE = 48.0f;
		
		private List<Float> values;
		
		private int dirty = 0;
		
		private float minValue = INIT_MIN_VALUE;
		private float maxValue = INIT_MAX_VALUE;
		private float mediumValue = (minValue + maxValue) / 2;
		private int deltaX;
		
		private int width;
		private int height;
		
		private float songBpm;
		private float userBpm;
		
		private float lastSlope;
		private double cycle;
		
		private boolean running;
		
		private Paint greenPaint;
		private Paint yellowPaint;
		
		private SurfaceHolder surfaceHolder;

		private BeatListener beatListener;
		
		public SongThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
			this.surfaceHolder = surfaceHolder;
			
			this.values = new LinkedList<Float>();
			
			this.greenPaint = new Paint();
			greenPaint.setAntiAlias(true);
			greenPaint.setARGB(255, 0, 255, 0);
			greenPaint.setStrokeWidth(3);
			
			this.yellowPaint = new Paint();
			yellowPaint.setAntiAlias(true);
			yellowPaint.setARGB(255, 255, 255, 0);
			yellowPaint.setStrokeWidth(1);

			yellowPaint.setTextSize(FONT_SIZE);
			yellowPaint.setTextAlign(Align.LEFT);
		}
		
		@Override
		public void run() {
			while(this.running)
			{
	            Canvas canvas = null;
	            try {
	            	canvas = this.surfaceHolder.lockCanvas(null);
	                synchronized (this.surfaceHolder) {                    
	                    doDraw(canvas);
	                }
	            } finally {
	                // do this in a finally so that if an exception is thrown
	                // during the above, we don't leave the Surface in an
	                // inconsistent state
	                if (canvas != null) {
	                	this.surfaceHolder.unlockCanvasAndPost(canvas);
	                }
	            }	
			}
		}
		
		public void setRunning(boolean running)
		{
			this.running = running;
		}
		
		private void doDraw(Canvas canvas)
		{
			if(this.dirty > 0)
			{
				canvas.drawARGB(255, 0, 0, 0);
				
				Point point = this.getPoint(this.values.get(this.values.size() - 1), 0);				
				canvas.drawLine(point.x, point.y, this.width, point.y, this.greenPaint);
				
				// Draw line in the middle between max and min values
				point = this.getPoint(this.mediumValue, 0);
				canvas.drawLine(point.x, point.y, this.width, point.y, this.yellowPaint);
				
				// Draw the treshold lines
				point = this.getPoint(this.mediumValue + TAP_THRESHOLD_VALUE, 0);
				canvas.drawLine(point.x, point.y, this.width, point.y, this.yellowPaint);
				
				point = this.getPoint(this.mediumValue - TAP_THRESHOLD_VALUE, 0);
				canvas.drawLine(point.x, point.y, this.width, point.y, this.yellowPaint);
				
				// Draw bpm values
				canvas.drawText(String.format("%10.2f", this.songBpm), 0, FONT_SIZE, this.yellowPaint);
				canvas.drawText(String.format("%10.2f", this.userBpm), 0, FONT_SIZE * 2, this.yellowPaint);
				canvas.drawText(String.format("%10.2f", this.userBpm / this.songBpm), 0, FONT_SIZE * 3, this.yellowPaint);
				
				canvas.save();
				
				this.dirty--;
			}
		}
		
		public void setDimensions(int w, int h)
		{
			this.width = w;
			this.height = h;
			
			this.deltaX = (int)this.width / MAX_VALUES;
		}

		public void addValue(float value)
		{
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
			
			this.dirty = 2;
			
			this.detectCycle(value);
		}
		
		private void updateMediumValue()
		{
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
		
		public float slope()
		{
			int count = this.values.size();
			float slope = 0;
			
			if(count >= 2)
			{
				slope = (float)(this.values.get(count - 1) - this.values.get(count - 2));
			}
			
			return slope;
		}
		
		/**
		 * Finds the x and y coords for the nth value
		 * in the values list.
		 * @param index
		 * @return Point object
		 */
		@SuppressWarnings("unused")
		private Point getPoint(int index)
		{
			double value = this.values.get(index);

			int x = (this.deltaX * index);
			
			return this.getPoint(value, x);
		}
		
		/**
		 * Finds the x and y coords for the nth value
		 * in the values list.
		 * @param value
		 * @param x
		 * @return
		 */
		private Point getPoint(double value, int x)
		{
			int y = 0;
			double diff = this.maxValue - this.minValue;

			if(diff > 0)
			{
				y = (int)(((this.maxValue - value) / diff) * (double)this.height);
			}

			return new Point(x, y);			
		}

		public void setBeatListener(BeatListener beatListener) {
			this.beatListener = beatListener;
		}
		
		public void setSongBpm(float bpm)
		{
			this.songBpm = bpm;
		}
		
		public void setUserBpm(float bpm)
		{
			this.userBpm = bpm;
		}
	}
	
	/** The thread that actually draws the animation */
	private SongThread thread;		
	
    public SongView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new SongThread(holder, context, null);
    }

	public void surfaceChanged(SurfaceHolder arg0, int format, int width, int height) {
		this.thread.setDimensions(width, height);		
	}

	public void surfaceCreated(SurfaceHolder arg0) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
		thread.setRunning(true);
		if(!thread.isAlive())
		{
			thread.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		thread.setRunning(false);

		boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } 
            catch (InterruptedException e) {}
        }		
	}
	
	public SongThread getThread()
	{
		return this.thread;
	}
}
