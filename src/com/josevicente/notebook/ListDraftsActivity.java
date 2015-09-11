package com.josevicente.notebook;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ListDraftsActivity extends BaseActivity{
	private static final String TAG = ListDraftsActivity.class.getSimpleName();
	SQLiteDatabase db;
	Cursor cursor;
	LinearLayout layout;
	TextView titleActivity,titleNote,txtNote,createdAtNote;
	ListView listDrafts;
	SimpleCursorAdapter adapter;
	String OkDelete,ErrorDelete;
	static String created;
	static final String[] FROM = { NoteBookData.C_TITLE, NoteBookData.C_TEXT, NoteBookData.C_CREATED_AT };
	static final int[] TO = { R.id.textTitle, R.id.textNote, R.id.textCreatedAt };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreated");
		setContentView(R.layout.listdrafts);
		// Instanciamos el layout
		layout = (LinearLayout)findViewById(R.id.layoutListDrafts);
		notebook.setPrefLinearLayout(layout); // Filtra las preferencias
		// Encontramos las referencias ListView y TextView
		titleActivity = (TextView)findViewById(R.id.titleActivity);
		listDrafts = (ListView) findViewById(R.id.listDrafts);
		listDrafts.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor", "WHITE")));
		created = getResources().getString(R.string.created);
		OkDelete = this.getResources().getString(R.string.okDeleteDraft);
		ErrorDelete = this.getResources().getString(R.string.errorDeleteDraft);
		// Filtramos los textView por las preferencias solo el título
		notebook.setPrefTextView(titleActivity,"titleActivity");		
	}
	@Override
	public void finish() {
		notebook.getNoteBookData().close();//Cerramos la base de datos;
		super.finish();
	}
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			startActivity(new Intent(this, MyNotesActivity.class));
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		try{
			// Tarea asincrona para leer los datos de la BBDD
			ReadData read = new ReadData();
			read.execute();
			cursor = read.get();// Pasa el cursor del la tarea asincrona
			startManagingCursor(cursor);
			adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
			listDrafts.setAdapter(adapter);
			// Register the ListView  for Context menu  
	        registerForContextMenu(listDrafts);
			adapter.setViewBinder(VIEW_BINDER);
			listDrafts.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				//	Cambia el color de la fuente y el fondo al seleccionar un cursor
				TextView titleNote = (TextView)view.findViewById(R.id.textTitle);
				TextView txtNote = (TextView)view.findViewById(R.id.textNote);
				TextView txtCreatedAt = (TextView)view.findViewById(R.id.textCreatedAt);
				titleNote.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				titleNote.setTextColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
				txtNote.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				txtNote.setTextColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
				txtCreatedAt.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				txtCreatedAt.setTextColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
				// Creamos un nuevo Intent
				Intent viewDraft = new Intent();
				// Pasa los datos en un Bundle desde ListDraftsActivity al siguiente Activity -> ViewDraftActivity
				Bundle bundle = new Bundle();
				bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
				bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
				bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
				bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
				viewDraft.putExtras(bundle);
				viewDraft.setClass(ListDraftsActivity.this,ViewDraftActivity.class); // Siguiente Activity
				startActivity(viewDraft); // Comienza la actividad ViewNoteActivity
				finish();
			}
	      });
		}catch(Exception e){
			Log.i(TAG,e.toString());
		}
	}
	// Añadimos la lógica de negocio
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() == R.id.textCreatedAt){
			// 	Actualizamos el tiempo
				long timestamp = cursor.getLong(columnIndex);
				CharSequence realTime = DateUtils.getRelativeTimeSpanString(view.getContext(),timestamp);
				((TextView) view).setText(created + ": " + realTime);
				((TextView) view).setTextColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				((TextView) view).setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
				((TextView) view).setTextSize(Integer.valueOf(notebook.getPrefs().getString("fontSize","16")).floatValue()-6);
				((TextView) view).setTypeface(Typeface.create(notebook.getPrefs().getString("fontStyle", "default"),Typeface.NORMAL));
				return true;
			}else if(view.getId() == R.id.textNote){
				String txtNote = cursor.getString(columnIndex);
				((TextView) view).setText(string20(txtNote));
				((TextView) view).setTextColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				((TextView) view).setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
				((TextView) view).setTextSize(Integer.valueOf(notebook.getPrefs().getString("fontSize","16")).floatValue()-2);
				((TextView) view).setTypeface(Typeface.create(notebook.getPrefs().getString("fontStyle", "default"),Typeface.NORMAL));			
				return true;
			}else if(view.getId() == R.id.textTitle){
				String txtTitle = cursor.getString(columnIndex);
				((TextView) view).setText(string20(txtTitle));
				((TextView) view).setTextColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				((TextView) view).setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
				((TextView) view).setTextSize(Integer.valueOf(notebook.getPrefs().getString("fontSize","16")).floatValue()-2);
				((TextView) view).setTypeface(Typeface.create(notebook.getPrefs().getString("fontStyle", "default"),Typeface.BOLD));
				((TextView) view).setDrawingCacheBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
				return true;
			}
			return false;
		}
	};
	// Esta función es un Helper para acortar un string
	private static String string20(String str){
		if(str!=null){
			if(str.length()>21 )
				return str.substring(0,20)+"...";
			else if(str.length()==0){
				return "(Sin contenido)";
				}
				else{
					return str;
				}
		}
		return "(Sin contenido)";
	}
	// ContextMenu al dejar pulsado un cursor
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.titleQMenu);
		menu.add(0, v.getId(), 0, R.string.see);//groupId, itemId, order, title
		menu.add(0, v.getId(), 0, R.string.delete);
	}
	// Acciones del contextMenu
	@Override
	public boolean onContextItemSelected(MenuItem item){
		if(item.getTitle()==getResources().getString(R.string.see)){ // Ver el borrador
			Log.i(TAG,item.getTitle().toString());
			Log.d(TAG,Integer.toString(cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID))));
			// Creamos un nuevo Intent
			Intent viewDraft = new Intent();
			// Pasa los datos en un Bundle desde MyNotesActivity al siguiente Activity -> ViewActivity
			Bundle bundle = new Bundle();
			bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
			bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
			bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
			bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
			viewDraft.putExtras(bundle);
			viewDraft.setClass(ListDraftsActivity.this,ViewDraftActivity.class); // Siguiente Activity
			startActivity(viewDraft); // Comienza la actividad ViewNoteActivity
			finish();
		}else if(item.getTitle()==getResources().getString(R.string.delete)){ // Eliminar el borrador
			Log.i(TAG,item.getTitle().toString());
			Log.d(TAG,Integer.toString(cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID))));
			DeleteData delete = new DeleteData();// Tarea asincrona
			Integer[] id = new Integer[1];
			id[0] = cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID));
			delete.execute(id);// pasa el id de la nota a eliminar
			try {
				if(delete.get()){// Devuelve true si se ha eliminado la nota en la tarea asincrona
					// Si eliminamos los datos, comenzamos otro activity MyNotesActivity
					Intent listDrafts = new Intent();
					listDrafts.setClass(ListDraftsActivity.this,ListDraftsActivity.class);
					startActivity(listDrafts); // Comienza la actividad MyNotesActivity
					finish();// Termina la actividad ViewNoteActivity
				}
			} catch (Exception e) {
				Log.i(TAG,e.toString());
			}
		}
		return true;
	} 
	class ReadData extends AsyncTask<Void, Integer, Cursor> {
		// Llamada al empezar
		Cursor cursorAsyn;
		@Override
		protected Cursor doInBackground(Void... nothing) {
			try {
				// Conectamos con la base de datos para lectura
				db = notebook.getNoteBookData().getDataBase().getReadableDatabase();
				// Obtenemos los datos de la tabla borradores
				cursorAsyn = db.query(NoteBookData.TABLE_DRAFTS, null, null, null, null, null,NoteBookData.C_CREATED_AT + " DESC");
				return cursorAsyn;
			}catch(Exception e){
				Log.i(TAG,e.toString());
				return cursorAsyn = null;
			}
		}
	}
	// Tarea asincrona para eliminar la nota
	class DeleteData extends AsyncTask<Integer, Integer, Boolean> {
		// Llamada al empezar
		@Override
		protected Boolean doInBackground(Integer... id) {
			Boolean bool = false;
			if(notebook.getNoteBookData().deleteDraftById(id[0]));// Elimina el borrador
				bool = true;
			return bool;
		}
		// Llamada cuando la actividad en background ha terminado
		@Override
		protected void onPostExecute(Boolean result) { //
			// Acción al completar la actualización del estado
			if(result){
				Toast.makeText(ListDraftsActivity.this, OkDelete,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(ListDraftsActivity.this, ErrorDelete,Toast.LENGTH_LONG).show();
			}			
		}
	}
}
