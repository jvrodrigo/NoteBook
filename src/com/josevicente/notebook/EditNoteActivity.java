package com.josevicente.notebook;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditNoteActivity extends BaseActivity implements OnClickListener,TextWatcher{
	//NoteBookData noteBookData;
	Button btnSave;
	SQLiteDatabase db;
	Bundle bundle;
	LinearLayout layout;
	TextView textCount,titleActivity;
	EditText txtNote,titleNote;
	String yes,no,errorEdit,OkEdit,Error;
	Cursor cursor;
	int _id;
	final static String TAG = EditNoteActivity.class.getSimpleName();
	@Override
	public void finish(){
		notebook.getNoteBookData().close();//Cerramos la base de datos;
		super.finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreated");
		super.onCreate(savedInstanceState);
		//noteBookData = new NoteBookData(this);
		// Creamos los contenedores para pasarlos al layout viewnote.xml
		setContentView(R.layout.editnote);
		// Instanciamos el layout
		layout = (LinearLayout)findViewById(R.id.layoutEditNote);
		notebook.setPrefLinearLayout(layout); // Filtramos las preferencias
		titleActivity = (TextView)findViewById(R.id.titleActivity);
		titleNote = (EditText)findViewById(R.id.editTitleNote);
		txtNote = (EditText)findViewById(R.id.editTxtNote);
		OkEdit = this.getResources().getString(R.string.OkEdit);
		errorEdit = this.getResources().getString(R.string.ErrorEdit);
		//TextView textCreatedAt = (TextView)findViewById(R.id.editTextCreatedAt);
		// Obtenemos los datos del anterior activity -> ViewNoteActivity
		bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		_id = bundle.getInt(NoteBookData.C_ID);
		// Pasamos los datos a los EditView
		titleNote.setText(bundle.getString(NoteBookData.C_TITLE));
		txtNote.setText(bundle.getString(NoteBookData.C_TEXT));
		// Botones
		btnSave = (Button)findViewById(R.id.buttonSave);
		btnSave.setOnClickListener(this);
		// Contador TextWatcher
		textCount = (TextView) findViewById(R.id.editTextCount);
		int count = txtNote.length();
		textCount.setText(Integer.toString(count));
		// Pasamos los TextView por la preferencias
		notebook.setPrefTextView(titleActivity, "titleActivity");
		notebook.setPrefTextView(titleNote,NoteBookData.C_TITLE);
		notebook.setPrefTextView(txtNote,NoteBookData.C_TEXT);
		notebook.setPrefTextView(textCount, "textCount");
		//Listener TextWatcher
		txtNote.addTextChangedListener(this);
	}
	
	@Override
	// Configuracion del boton atrás
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			Intent viewnote = new Intent();
			viewnote.putExtras(bundle);// Pasamos el bundle a la actividad ViewNote
			viewnote.setClass(EditNoteActivity.this,ViewNoteActivity.class);
			startActivity(viewnote); // Comienza la actividad ViewNoteActivity
			finish();// terminamos la actividad EditNote
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void afterTextChanged(Editable v) {
		int count = v.length();
		textCount.setText(Integer.toString(count));
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick(View arg0) {
		if(titleNote.getText().length()==0){// Si la nota no tiene titulo
			Toast.makeText(EditNoteActivity.this, getResources().getString(R.string.toastInputTitle),Toast.LENGTH_LONG).show();
		}
		if(txtNote.getText().length()==0){// Si la nota no tiene texto
			Toast.makeText(EditNoteActivity.this, getResources().getString(R.string.toastInputTxt),Toast.LENGTH_LONG).show();
		}
		if(txtNote.getText().length()!=0 && titleNote.getText().length()!=0){
			try{
				// Tarea asincrona UpdateData();						 
				UpdateData update = new UpdateData();
				update.execute();
				cursor = update.get();// Obtiene el cursor de la tarea Asincrona
				cursor.moveToFirst();
				// Creamos dos nuevos Intent
				Intent viewNote = new Intent();
				// 	Pasa los datos de la nota al siguiente activity -> View
				Bundle bundle = new Bundle();
				bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
				bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
				bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
				bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
				viewNote.putExtras(bundle);
				viewNote.setClass(EditNoteActivity.this,ViewNoteActivity.class);
				startActivity(viewNote); // Comienza la actividad ViewNoteActivity para ver la nota
				finish(); // Termina la actividad EditNoteActivity
			}catch(Exception e){
				Log.i(TAG,e.toString());
			}
			
		}
	}
	class UpdateData extends AsyncTask<Void, Integer, Cursor> {
		// Llamada al empezar
		Cursor cursorAsyn;
		@Override
		protected Cursor doInBackground(Void... nothing) {
			try {
				// Get database
				db = notebook.getNoteBookData().getDataBase().getWritableDatabase();
				// Create a new map of values, where column names are the keys
				ContentValues values = new ContentValues();
				values.put(NoteBookData.C_TITLE, titleNote.getText().toString());								
				values.put(NoteBookData.C_TEXT,txtNote.getText().toString());
				db.update(NoteBookData.TABLE_NOTES, values, NoteBookData.C_ID+"="+ _id,null);
				// Nos aseguramos que el cambio se ha hecho, volviendo a consultar la BBDD
				String query = "SELECT * FROM "+NoteBookData.TABLE_NOTES +" WHERE _id = "+_id;
				cursorAsyn = db.rawQuery(query,null);
				return cursorAsyn;
			} catch (Exception e) {
				Log.i(TAG,"Error update data");
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
			// Acción al actualizar la nota
			if(result==null){
				Toast.makeText(EditNoteActivity.this, errorEdit,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(EditNoteActivity.this, OkEdit,Toast.LENGTH_LONG).show();
			}
		}
	}
}
