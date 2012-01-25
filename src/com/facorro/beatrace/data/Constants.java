package com.facorro.beatrace.data;

public class Constants {
	// DataBase name
	public static final String DATABASE_NAME = "BEATRACE";
	// DataBase name
	public static final int DATABASE_VERSION = 1;
	// Table name
	public static final String TABLE_NAME = "SONG_BPM";
	// Columns
	public static final String KEY_ID = "ID";
	public static final String ARTIST = "ARTIST";
	public static final String SONG_NAME = "SONG_NAME";
	public static final String FILENAME = "FILENAME";
	public static final String BPM = "BPM";
	// Creation script
	public static final String CREATE_TABLE = 
			"create table " +
			Constants.TABLE_NAME + " (" +
			Constants.KEY_ID + " integer primary key autoincrement, " +
			Constants.ARTIST + " text, " +
			Constants.SONG_NAME + " text, " +
			Constants.FILENAME + " text not null, " +
			Constants.BPM + " float);";
}
