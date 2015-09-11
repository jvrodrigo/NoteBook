package com.josevicente.notebook;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;

// Nueva nota
public class NewNoteActivity extends BaseActivity implements OnClickListener,TextWatcher{
	private static final String TAG = NewNoteActivity.class.getSimpleName();
	TextView textCount,titleActivity;
	Cursor cursor;
	SQLiteDatabase db;
	LinearLayout layout;
	Button btnSave;
	String yes,no,noteTitle,saveNote,Ok,Error,noSave;
	EditText input;
	AutoSaveService autoSaveService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreated");
		setContentView(R.layout.newnote);
		notebook.setFlagSave(false);// Ponemos la bandera a false porque no se ha guardado la nueva nota
		// Instanciamos el layout
		layout = (LinearLayout)findViewById(R.id.layoutNewNote);
		notebook.setPrefLinearLayout(layout);// Filtra las preferencias layout
		notebook.txtNote = (EditText)findViewById(R.id.txtNote);
		btnSave = (Button)findViewById(R.id.buttonSave);
		textCount = (TextView) findViewById(R.id.textCount);
		textCount.setText(Integer.toString(0));
		titleActivity = (TextView) findViewById(R.id.titleActivity);
		// Filtramos los TextView por la preferencias
		notebook.setPrefTextView(titleActivity, "titleActivity");
		notebook.setPrefTextView(textCount, "textCount");
		notebook.setPrefEditText(notebook.txtNote, NoteBookData.C_TEXT);
		// Recursos
		Ok = this.getResources().getString(R.string.saveOk);
		Error= this.getResources().getString(R.string.saveError);
		noSave = this.getResources().getString(R.string.noSave);
		yes = this.getResources().getString(R.string.yes);
		no = this.getResources().getString(R.string.no);
		noteTitle = this.getResources().getString(R.string.titleNewNote);
		saveNote = this.getResources().getString(R.string.saveNote);
		// Save botton
		btnSave.setOnClickListener(this);
		// Listener del texto
		notebook.txtNote.addTextChangedListener(this);
		// Iniciamos el AutoSaveService
		if(notebook.getPrefs().getBoolean("autoSaveService", false)==true){
			startService(new Intent(this, AutoSaveService.class));
			Toast.makeText(NewNoteActivity.this, getResources().getString(R.string.autoSaveOn),Toast.LENGTH_LONG).show();
		}
	}
	@Override 
	public void finish(){
		Log.i(TAG,"Finish");
		if(notebook.isServiceRunning()){ // Paramos el servicio autoSave
		notebook.setServiceRunning(false);}
		stopService(new Intent(this,AutoSaveService.class));
		try{
			notebook.getNoteBookData().close();//Cerramos la base de datos;
		}catch(SQLException e){
			Log.i(TAG,e.getStackTrace().toString());
		}
		super.finish();
	}
	@Override
	// Configuracion del boton atrás
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			notebook.setServiceRunning(false);
			startActivity(new Intent(this, MyNotesActivity.class));
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	// Listener del botón Guardar
	@Override
	public void onClick(View arg0) {
		if(notebook.txtNote.getText().length()==0){
			Toast.makeText(NewNoteActivity.this, getResources().getString(R.string.toastInputTxt),Toast.LENGTH_LONG).show();
		}else{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// Inflamos el layout para introducir otro layout del titulo de la nota
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View promptView = layoutInflater.inflate(R.layout.prompt, null);
		// set title
		input = (EditText) promptView.findViewById(R.id.titleInput); // Variable que almacenará el titulo
		// Crea un alert al ser pulsado para guardar la nota
		alertDialogBuilder.setView(promptView);
		alertDialogBuilder.setTitle(noteTitle);
		// set dialog message
		alertDialogBuilder
			.setMessage(saveNote)
			.setCancelable(false)
			.setPositiveButton(yes,new DialogInterface.OnClickListener() {// Si pulsa Si
				public void onClick(DialogInterface dialog,int id) {
				if(input.getText().length()==0){
					Toast.makeText(NewNoteActivity.this, getResources().getString(R.string.toastInputTitle),Toast.LENGTH_LONG).show();
				}else{
					
					
					try{
						// Tarea asincrona SaveData();						 
						SaveData save = new SaveData();
						save.execute();
						cursor = save.get();// Obtiene el cursor de la tarea Asincrona
						cursor.moveToFirst();
						Intent viewNote = new Intent();//Creamos un nuevo Intent para ver la nota guardada
						// Pasa los datos de la Nota guardada al siguiente activity -> View
						Bundle bundle = new Bundle();
						bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
						bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
						bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
						bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
						viewNote.putExtras(bundle);
						viewNote.setClass(NewNoteActivity.this,ViewNoteActivity.class);
						startActivity(viewNote); // Comienza la actividad ViewNoteActivity para ver la nota
						finish(); // Termina la actividad NewNoteActivity
					}catch(Exception ex){
						Toast.makeText(NewNoteActivity.this, Error,Toast.LENGTH_LONG).show();
						Log.i(TAG,ex.toString());
					}
				}
				}
			})
			.setNegativeButton(no,new DialogInterface.OnClickListener() { // Si pulsa no
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
					Toast.makeText(NewNoteActivity.this, noSave,Toast.LENGTH_LONG).show();
					dialog.cancel();
				}
			});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
		}
	}
	
	@Override
	public void afterTextChanged(Editable v) {
		int count = v.length();
		textCount.setText(Integer.toString(count));
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub	
	}
	// Guardar en la base de datos de manera asíncrona
	class SaveData extends AsyncTask<Void, Integer, Cursor> {
		// Llamada al empezar
		Cursor cursorAsyn;
		@Override
		protected Cursor doInBackground(Void... nothing) {
			try {
				// Obtiene la base de datos en modo escritura
				db = notebook.getNoteBookData().getDataBase().getWritableDatabase();
				Date date = new Date();
				// Create a new map of values, where column names are the keys
				ContentValues values = new ContentValues();
				// No hace falta incluir el ID porque se configura como integer autoincrement
				values.put(NoteBookData.C_TITLE, input.getText().toString());								
				values.put(NoteBookData.C_TEXT,notebook.txtNote.getText().toString());
				values.put(NoteBookData.C_CREATED_AT, date.getTime());
				// Inserta una nueva fila, devuelve el ID(primary key)de esta
				long newId = db.insert(NoteBookData.TABLE_NOTES,null,values);
				//Ponemos la bandera de guardado a true para eliminar el borrador
				notebook.setFlagSave(true);
				// Busca en la base de datos la ultima nota introducida
				String query = "SELECT * FROM "+NoteBookData.TABLE_NOTES +" WHERE _id = "+newId;
				cursorAsyn = db.rawQuery(query,null);// Pasa los datos de la consulta al cursor
				return cursorAsyn;
			} catch (Exception e) {
				
				Log.i(TAG,e.toString());
				return cursorAsyn=null;
			}
		}
		// Llamada cuando hay que actualizar algo en el proceso
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values); // No la usamos
		}
		// Llamada cuando la actividad en background ha terminado
		@Override
		protected void onPostExecute(Cursor result) { //
			// Acción al guardar la nota
			if(result==null){
				Toast.makeText(NewNoteActivity.this, Error,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(NewNoteActivity.this, Ok,Toast.LENGTH_LONG).show();
			}
		}
	}
}