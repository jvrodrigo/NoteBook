package com.josevicente.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity {
	private static final String TAG = PrefsActivity.class.getSimpleName();
	String msgDeleteAll,titleDeleteAll,yes,no,okDelteAll,errorDeleteAll;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) { //
		super.onCreate(savedInstanceState);
		msgDeleteAll = getResources().getString(R.string.msgDeleteAllNotes);
		titleDeleteAll = getResources().getString(R.string.titleDeleteAllNotes);
		okDelteAll = getResources().getString(R.string.okDeleteAll);
		errorDeleteAll = getResources().getString(R.string.errorDeleteAll);
		yes = this.getResources().getString(R.string.yes);
		no = this.getResources().getString(R.string.no);
		addPreferencesFromResource(R.xml.prefs);
		Preference myPref = (Preference) findPreference("deleteAllNotes");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {// Elimina todas las notas y borradores de base de datos
		             public boolean onPreferenceClick(Preference preference) {
		            	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PrefsActivity.this);
		            	 alertDialogBuilder.setTitle(titleDeleteAll);
		         		// set dialog message
		         		alertDialogBuilder
		         			.setMessage(msgDeleteAll)
		         			.setCancelable(false)
		         			.setPositiveButton(yes,new DialogInterface.OnClickListener() {// Si pulsa Si
		         				public void onClick(DialogInterface dialog,int id) {
		         					DeleteAllData deleteAll = new DeleteAllData();
		         					deleteAll.execute();
		         					Log.d(TAG,"All Detele");
		         				}
		         				})
		         				.setNegativeButton(no,new DialogInterface.OnClickListener() { // Si pulsa no
		         					public void onClick(DialogInterface dialog,int id) {
		         						// if this button is clicked, just close
		         						// the dialog box and do nothing
		         						dialog.cancel();
		         					}
		         				});
		         				// create alert dialog
		         				AlertDialog alertDialog = alertDialogBuilder.create();
		         				// show it
		         				alertDialog.show();
		     			return true;
		             }
		});
	}
	@Override
	// Configuracion del boton atrás
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			Toast.makeText(this, "Cerrando preferencias",Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, MyNotesActivity.class));
			finish();
			return true;
	}
		return super.onKeyDown(keyCode, event);
	}
	class DeleteAllData extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
				SQLiteDatabase db;
	         	db = BaseActivity.notebook.getNoteBookData().getDataBase().getWritableDatabase(); // Cogemos la base de datos y la eliminamos
	         	db.delete(NoteBookData.TABLE_NOTES,null,null);// Para borrar los notas
	         	db.delete(NoteBookData.TABLE_DRAFTS,null,null);// Para borrar las borradores
	         	db.close();
	         	return true;
			}catch(Exception e){
				Log.i(TAG,e.toString());
				return false;
			}
		}
		// Llamada cuando la actividad en background ha terminado
		@Override
		protected void onPostExecute(Boolean result) { //
			// Acción al completar el guardado de la nota
			if(result){
				Toast.makeText(PrefsActivity.this, okDelteAll,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(PrefsActivity.this, errorDeleteAll,Toast.LENGTH_LONG).show();
			}
		}
	}
}
