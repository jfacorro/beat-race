package com.facorro.beatrace.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataAccessHelper extends SQLiteOpenHelper {
	public DataAccessHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(Constants.CREATE_TABLE);
		} 
		catch(SQLiteException ex) {
			
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + Constants.TABLE_NAME);
		onCreate(db);		
	}

}
