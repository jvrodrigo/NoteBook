package com.josevicente.notebook;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteBookData {
	private static final String TAG = NoteBookData.class.getSimpleName();
	static final int VERSION = 1;
	static final String DATABASE = "notebook.db";
	static final String TABLE_NOTES = "notes";
	static final String TABLE_DRAFTS ="drafts";
	static final String C_ID = "_id";
	static final String C_CREATED_AT = "created_at";
	static final String C_TEXT = "txt";
	static final String C_TITLE = "title";
	private final DbHelper dbHelper;
	public NoteBookData(Context context) {
		this.dbHelper = new DbHelper(context);
		Log.i(TAG, "Initialized data");
	}
	public void close() {
		this.dbHelper.close();
	}
	public boolean deleteById(int id){
		try{
			this.dbHelper.getWritableDatabase().delete(TABLE_NOTES,C_ID+"="+id, null);
			Log.i(TAG,"Ok delete " + id);
			return true;
		}catch(SQLException ex){
			Log.i(TAG,"Error delete");
			return false;
		}
	}
	public boolean deleteDraftById(int id){
		try{
			this.dbHelper.getWritableDatabase().delete(TABLE_DRAFTS,C_ID+"="+id, null);
			Log.i(TAG,"Ok delete draft " + id);
			return true;
		}catch(SQLException ex){
			Log.i(TAG,"Error delete");
			return false;
		}
	}
	public DbHelper getDataBase(){
		return this.dbHelper;
	}
	public class DbHelper extends SQLiteOpenHelper {
		//Constructor
		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}
//	 	Llamadado para crear la tabla
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database: " + DATABASE);
			String sqlTableNotes = "create table " + TABLE_NOTES + " (" + C_ID + " integer primary key autoincrement, " +
					C_CREATED_AT + " int," + C_TITLE + " text, " + C_TEXT + " text)";
			db.execSQL(sqlTableNotes);
			String sqlTableDrafts = "create table " + TABLE_DRAFTS + " (" + C_ID + " integer primary key autoincrement, " +
					C_CREATED_AT + " int," + C_TITLE + " text, " + C_TEXT + " text)";
			db.execSQL(sqlTableDrafts);
		}
		// Llamado siempre que tengamos una nueva version
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Aqui van las sentencias del tipo ALTER TABLE, de momento lo hacemos mas sencillo:
			db.execSQL("drop table if exists " + TABLE_NOTES); // borra la vieja base de datos
			Log.d(TAG, "onUpdated");
			onCreate(db); // crea una base de datos nueva
		}
	}
}