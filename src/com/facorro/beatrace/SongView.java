package com.facorro.beatrace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SongView extends SurfaceView implements SurfaceHolder.Callback {
	class SongThread extends Thread {
		
		public int width;
		public int height;
		
		public Paint linePaint;
		public Paint bkgPaint;
		
		public SurfaceHolder surfaceHolder;
		public Context context;
		
		public SongThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
			this.surfaceHolder = surfaceHolder;
			this.context = context;
			
			this.linePaint = new Paint();
			linePaint.setAntiAlias(true);
			linePaint.setARGB(255, 255, 0, 0);
			
			this.bkgPaint = new Paint();
			bkgPaint.setARGB(0, 0, 255, 0);
		}
		
		@Override
		public void run() {
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
		
		private void doDraw(Canvas canvas)
		{
			canvas.drawRect(0, 0, this.width, this.height, this.bkgPaint);

			canvas.drawLine(0, 0, this.width, this.height, this.linePaint);
			
			canvas.save();
		}
		
		public void setDimensions(int w, int h)
		{
			this.width = w;
			this.height = h;
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

        setFocusable(true); // make sure we get key events
    }

	public void surfaceChanged(SurfaceHolder arg0, int format, int width, int height) {
		this.thread.setDimensions(width, height);		
	}

	public void surfaceCreated(SurfaceHolder arg0) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.start();		
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		
		boolean retry = true;
        
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }		
	}
}
