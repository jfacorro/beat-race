package com.facorro.beatrace.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DataAccess {
	private DataAccessHelper helper;
	private SQLiteDatabase database;
	private Context context;
	
	public DataAccess(Context context) {
		this.context = context;
		helper = new DataAccessHelper(this.context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}
	
	public void open() {
		try {
			database = helper.getWritableDatabase();
		} catch(SQLiteException ex) {
			database = helper.getReadableDatabase();
		}
	}
	
	public void close() {
		database.close();
	}
	
	public float getSongBpm(String filename) {
		float bpm = 0;

		Cursor result = database.query(Constants.TABLE_NAME, 
				new String[]{ Constants.BPM }, 
				Constants.FILENAME + " LIKE ?", 
				new String[]{ filename }, 
				null, null, null);

		if(result.moveToFirst()) {
			bpm = result.getFloat(0);
		}

		return bpm;
	}

	public void saveSongBpm(String filename, float songBpm) {
		Cursor result = database.query(Constants.TABLE_NAME, 
				new String[]{ Constants.BPM }, 
				Constants.FILENAME + " LIKE ?", 
				new String[]{ filename }, 
				null, null, null);
	
		if(!result.moveToFirst()) {
			ContentValues values = new ContentValues();
			values.put(Constants.FILENAME, filename);
			values.put(Constants.BPM, songBpm);

			database.insert(Constants.TABLE_NAME, null, values);
		} else {
			ContentValues values = new ContentValues();
			values.put(Constants.BPM, songBpm);

			database.update(Constants.TABLE_NAME, 
					values, 
					Constants.FILENAME + " LIKE ?", 
					new String[]{ filename }
				);
		}			
	}
}
