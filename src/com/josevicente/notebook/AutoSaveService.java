package com.josevicente.notebook;

import java.util.Date;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class AutoSaveService extends Service {
	static final String TAG = AutoSaveService.class.getSimpleName();;
	static final int DELAY = 5000; // 5 segundos segundo
	public boolean runFlag = false;
	private AutoSave autoSave;
	private NoteBookApplication notebook;
	public IBinder onBind(Intent intent) { //
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		this.notebook = (NoteBookApplication) getApplication();
		this.autoSave = new AutoSave();
		Log.d(TAG, "onCreated");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		this.runFlag = true;
		this.notebook.setServiceRunning(true);
		this.autoSave.start();
		Log.d(TAG, "onStarted");
		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		this.notebook.setServiceRunning(false);
		this.autoSave.interrupt();
		this.autoSave = null;
		Log.d(TAG, "onDestroyed");
	}
	/**
	* Thread that performs the actual update from the online service */
	private class AutoSave extends Thread {
		public AutoSave() {
			super("AutoSaveService");
		}
		@Override
		public void run() { //
			boolean exit=false;
			// Gets the data repository in write mode
			NoteBookData dbnote = notebook.getNoteBookData();
			
			SQLiteDatabase db = dbnote.getDataBase().getWritableDatabase();
			//Log.d(TAG, "Data base: "+dbnote.toString()+"->"+db.toString());
			Date date = new Date();
			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			values.put(NoteBookData.C_TITLE, "(Borrador)");	
			values.put(NoteBookData.C_TEXT,notebook.txtNote.getText().toString());
			values.put(NoteBookData.C_CREATED_AT, date.getTime());
			int id_draft=0;
			boolean isSaved=false;// Bandera para insertar por primera vez los datos del borrador, cuando se ha escrito algo en la nota
			//Cursor cursor;
			while (notebook.isServiceRunning()  && exit==false) {
				Log.d(TAG, "AutoSave running");
				try {
					if(notebook.txtNote.getText().length()!=0){
						if(isSaved==false){// Si no se ha guardado
							isSaved=true;
							//Log.d(TAG, "Entrando la primera vez bool:"+ isSaved);
							id_draft = (int) db.insert(NoteBookData.TABLE_DRAFTS,null,values);//Guarda en la tabla de borradores							
							values.put(NoteBookData.C_ID, id_draft);// Guarda el id del borrador
						}
						// No hace falta incluir el ID porque se configura como integer autoincrement
						values.put(NoteBookData.C_TEXT,notebook.txtNote.getText().toString());
						values.put(NoteBookData.C_CREATED_AT, date.getTime());
						//Log.i(TAG,notebook.txtNote.getText().toString());
						//Log.d(TAG, "Service Save Data: "+notebook.txtNote.getText().toString());
						db.update(NoteBookData.TABLE_DRAFTS, values, NoteBookData.C_ID+"="+ id_draft,null);// Actualiza la tabla de borradores
					}
					//Log.d(TAG, "Service running AutoSave, id: "+ id_draft);
					Thread.sleep(DELAY);// Duerme el hilo
				} catch (Exception e) {
					Log.d(TAG,"Interrupted SERVICE");
					exit = true;//Sale del bucle while
				}
				if(notebook.getFlagSave()){// Si hemos guardado la nota, eliminamos el borrador autoguardado
					//db.delete(NoteBookData.TABLE_DRAFTS, NoteBookData.C_ID+"="+id_draft, null);
					notebook.getNoteBookData().deleteDraftById(id_draft);
					//Log.d("AutoSave", "Draft "+ id_draft+" have been deleted");
					notebook.setFlagSave(false);// Ponemos a falso, para que la proxima vez entre en la condicion
				}
			}
			Log.d(TAG, "End SERVICE");
			db.close();
			isSaved=false;
		}
	}
}