package com.facorro.beatrace;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SongView extends SurfaceView implements SurfaceHolder.Callback {
	class SongThread extends Thread {
		private static final int MAX_VALUES = 50;
		private static final double INIT_MIN_VALUE = -10.0f;
		private static final double INIT_MAX_VALUE = 10.0f;
		
		private List<Double> values;
		
		private int dirty = 0;
		
		private double minValue = INIT_MIN_VALUE;
		private double maxValue = INIT_MAX_VALUE;
		private double deltaX;
		
		private int width;
		private int height;
		
		private boolean running;
		
		public Paint linePaint;
		public Paint bkgPaint;
		
		public SurfaceHolder surfaceHolder;
		public Context context;
		private int lastSlope;
		private double cycle;
		private boolean beat;
		
		public SongThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
			this.surfaceHolder = surfaceHolder;
			this.context = context;
			
			this.values = new LinkedList<Double>();
			
			this.linePaint = new Paint();
			//linePaint.setAntiAlias(true);
			linePaint.setARGB(255, 255, 255, 0);
			linePaint.setStrokeWidth(2);
			
			this.bkgPaint = new Paint();
			bkgPaint.setARGB(255, 0, 0, 0);
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
				
				//float [] points = new float[this.values.size()];
				
				for (int index = 0; index + 2 < this.values.size(); index++)
				{
					Point p1 = this.getPoint(index);
					Point p2 = this.getPoint(index + 1);
					//points[index]
					
					canvas.drawLine(p1.x, p1.y, p2.x, p2.y, this.linePaint);
				}
				
				Point zeroBegin = this.getPoint(0, 0);
				Point zeroEnd = new Point(this.width, zeroBegin.y);
				
				canvas.drawLine(zeroBegin.x, zeroBegin.y, zeroEnd.x, zeroEnd.y, this.linePaint);
				
				canvas.save();
				
				this.dirty--;
			}
		}
		
		public void setDimensions(int w, int h)
		{
			this.width = w;
			this.height = h;
			
			this.deltaX = (float)this.width / MAX_VALUES;
		}

		public void addValue(double value)
		{
			if(value < this.minValue)
			{
				this.minValue = value;
			}
			
			if(value > this.maxValue)
			{
				this.maxValue = value;
			}

			if(this.values.size() >= MAX_VALUES)
			{
				this.values.remove(0);
			}

			this.values.add(value);
			
			this.dirty = 2;
			
			this.detectCycle();
		}
		
		private void detectCycle() {
			if(this.lastSlope != this.slope())
			{
				this.lastSlope = this.slope();
				this.cycle += 1;
			}
			
			if(this.cycle == 2)
			{
				this.beat = true;
				this.cycle = 0;
			}
		}
		
		public boolean beat()
		{
			boolean result = this.beat;
			this.beat = false;
			return result;
		}
		
		public int slope()
		{
			int count = this.values.size();
			int slope = 0;
			
			if(count > 2)
			{
				if(this.values.get(count - 1) > this.values.get(count - 2))
				{
					slope = 1;
				}
				else if(this.values.get(count - 1) < this.values.get(count - 2))
				{
					slope = -1;
				}
				else
				{
					slope = 0;
				}
			}
			
			return slope;
		}
		
		private Point getPoint(int index)
		{
			double value = this.values.get(index);

			int x = (int)(this.deltaX * (double)index);
			
			return this.getPoint(value, x);
		}
		
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
	}
	
	/** The thread that actually draws the animation */
	private SongThread thread;		
	
    public SongView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new SongThread(holder, context, new Handler(){
        	
        });

        //setFocusable(true); // make sure we get key events
    }

	public void surfaceChanged(SurfaceHolder arg0, int format, int width, int height) {
		this.thread.setDimensions(width, height);		
	}

	public void surfaceCreated(SurfaceHolder arg0) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
		thread.setRunning(true);
        thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		
		boolean retry = true;
		thread.setRunning(false);        
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }		
	}

	public void addValue(double value) {
		this.thread.addValue(value);
	}
	
	public int slope()
	{
		return this.thread.slope();
	}

	public boolean beat() {
		return this.thread.beat();
	}


}
