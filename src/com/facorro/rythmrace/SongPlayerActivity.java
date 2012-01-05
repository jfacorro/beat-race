package com.facorro.rythmrace;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.TextView;

public class SongPlayerActivity extends Activity {
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_player);
        
        Paint paint = new Paint();
        paint.setColor(0);
        Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawRect(new Rect(0, 0, 10, 10), paint);
        
        Intent intent = this.getIntent();
        
        TextView text = (TextView) this.findViewById(R.id.txtTest);
        String filename = intent.getStringExtra("filename");
        text.setText(filename);
	}
}
