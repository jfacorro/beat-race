package com.facorro.beatrace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SongPlayerActivity extends Activity {
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_player);
        
        Intent intent = this.getIntent();
        
        TextView text = (TextView) this.findViewById(R.id.txtTest);
        
        //SurfaceView view = (SurfaceView) this.findViewById(R.id.surfaceView);
        
        String filename = intent.getStringExtra("filename");
        text.setText(filename);
	}
}
