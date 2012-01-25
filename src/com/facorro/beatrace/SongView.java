package com.facorro.beatrace;

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
import com.facorro.beatrace.utils.CycleBeatDetection;

public class SongView extends SurfaceView implements SurfaceHolder.Callback {
	class SongThread extends Thread {
		private static final float FONT_SIZE = 48.0f;
		
		private int dirty = 0;
		
		private int width;
		private int height;
		
		private float songBpm;
		private float userBpm;
		
		private boolean running;
		
		private Paint greenPaint;
		private Paint yellowPaint;
		
		private SurfaceHolder surfaceHolder;
		
		private BeatDetection beatDetection;

		public SongThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
			this.surfaceHolder = surfaceHolder;
			
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
		
		private void setRunning(boolean running)
		{
			this.running = running;
		}
		
		private void doDraw(Canvas canvas)
		{
			if(this.dirty > 0 && this.beatDetection != null)
			{
				canvas.drawARGB(255, 0, 0, 0);
				
				List<Float> values = beatDetection.getValues();
				float mediumValue = beatDetection.getMedium();
				
				Point point = this.getPoint(values.get(values.size() - 1), 0);				
				canvas.drawLine(point.x, point.y, this.width, point.y, this.greenPaint);
				
				// Draw line in the middle between max and min values
				point = this.getPoint(mediumValue, 0);
				canvas.drawLine(point.x, point.y, this.width, point.y, this.yellowPaint);
				
				// Draw the treshold lines
				point = this.getPoint(mediumValue + CycleBeatDetection.TAP_THRESHOLD_VALUE, 0);
				canvas.drawLine(point.x, point.y, this.width, point.y, this.yellowPaint);
				
				point = this.getPoint(mediumValue - CycleBeatDetection.TAP_THRESHOLD_VALUE, 0);
				canvas.drawLine(point.x, point.y, this.width, point.y, this.yellowPaint);

				// Draw bpm values
				canvas.drawText(String.format("%10.2f", this.songBpm), 0, FONT_SIZE, this.yellowPaint);
				canvas.drawText(String.format("%10.2f", this.userBpm), 0, FONT_SIZE * 2, this.yellowPaint);
				canvas.drawText(String.format("%10.2f", this.userBpm / this.songBpm), 0, FONT_SIZE * 3, this.yellowPaint);
				
				canvas.save();
				
				this.dirty--;
			}
		}
		
		public void setBeatDetection(BeatDetection beatDetection) {
			this.beatDetection = beatDetection;
		}

		public void setSongBpm(float bpm)
		{
			this.songBpm = bpm;
			this.updateView();
		}
		
		public void setUserBpm(float bpm)
		{
			this.userBpm = bpm;
			this.updateView();
		}
		
		private void updateView()
		{
			this.dirty = 2;
		}

		private void setDimensions(int w, int h)
		{
			this.width = w;
			this.height = h;
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
			double diff = this.beatDetection.getMax() - this.beatDetection.getMin();

			if(diff > 0)
			{
				y = (int)(((this.beatDetection.getMax() - value) / diff) * (double)this.height);
			}

			return new Point(x, y);
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
