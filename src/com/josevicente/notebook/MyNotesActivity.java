package com.josevicente.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// Esta actividad muestra todas las notas creadas en filas.
public class MyNotesActivity extends BaseActivity{
	private static final String TAG = MyNotesActivity.class.getSimpleName();
	//NoteBookData noteBookData;
	SQLiteDatabase db;
	Cursor cursor;
	LinearLayout layout;//linearLayout;
	//RelativeLayout relativeLayout;
	CheckBox checkbox;
	TextView titleActivity;
	ListView listMyNotes;
	String yes, no,exit,exitApplication,OkDelete,ErrorDelete;
	SimpleCursorAdapter adapter;
	static String created;
	static final String[] FROM = {NoteBookData.C_TITLE, NoteBookData.C_TEXT, NoteBookData.C_CREATED_AT };
	static final int[] TO = {R.id.textTitle, R.id.textNote, R.id.textCreatedAt };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreated");
		setContentView(R.layout.mynotes);
		// Instanciamos el layout
		layout = (LinearLayout)findViewById(R.id.layoutMyNotes);
		
		// Encontramos las referencias ListView y TextView
		exit = this.getResources().getString(R.string.exit);
		exitApplication = this.getResources().getString(R.string.exitApplication);
		yes = this.getResources().getString(R.string.yes);
		no = this.getResources().getString(R.string.no);
		OkDelete = this.getResources().getString(R.string.OkDelete);
		ErrorDelete = this.getResources().getString(R.string.ErrorDelete);
		titleActivity = (TextView)findViewById(R.id.titleActivity);
		listMyNotes = (ListView) findViewById(R.id.listMyNotes);
		listMyNotes.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor", "WHITE")));
		created = getResources().getString(R.string.created);
		// Filtramos los textView y el layout por las preferencias
		notebook.setPrefLinearLayout(layout);
		notebook.setPrefTextView(titleActivity,"titleActivity");
		
	}
	@Override
	public void finish() {
		super.finish();
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
			listMyNotes.setAdapter(adapter);
			// Register the ListView  for Context menu  
	        registerForContextMenu(listMyNotes); 
			adapter.setViewBinder(VIEW_BINDER);
			listMyNotes.setOnItemClickListener(new OnItemClickListener() {
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
				Intent viewNote = new Intent();
				// Pasa los datos en un Bundle desde MyNotesActivity al siguiente Activity -> ViewActivity
				Bundle bundle = new Bundle();
				bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
				bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
				bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
				bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
				viewNote.putExtras(bundle);
				viewNote.setClass(MyNotesActivity.this,ViewNoteActivity.class); // Siguiente Activity
				startActivity(viewNote); // Comienza la actividad ViewNoteActivity
				finish();
			}
			});
			/*listMyNotes.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,int pos, long id) {
					TextView titleNote = (TextView)view.findViewById(R.id.textTitle);
					TextView txtNote = (TextView)view.findViewById(R.id.textNote);
					TextView txtCreatedAt = (TextView)view.findViewById(R.id.textCreatedAt);
					titleNote.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
					titleNote.setTextColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
					txtNote.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
					txtNote.setTextColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
					txtCreatedAt.setBackgroundColor(Color.parseColor(notebook.getPrefs().getString("fontColor","BLACK")));
					txtCreatedAt.setTextColor(Color.parseColor(notebook.getPrefs().getString("backgroundColor","WHITE")));
					return false;
				}
				
			});*/
		}catch(Exception e){
			Log.i(TAG,e.toString());
		}
	}
	@Override
	// Configuracion del boton atrás
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			AlertDialog.Builder alertDialogBuilderExit = new AlertDialog.Builder(this);
			// Creamos un Alert para Salir de la aplicación
			alertDialogBuilderExit.setTitle(exit);
			// set dialog message
			alertDialogBuilderExit
					.setMessage(exitApplication)
					.setCancelable(false)
					.setPositiveButton(yes,new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog,int id) {
							//Toast.makeText(MyNotesActivity.this, "Aplicación cerrada",Toast.LENGTH_SHORT).show();
							finish();
							android.os.Process.killProcess(android.os.Process.myPid());// Cerramos el proceso en el SO
						}						
					})
				.setNegativeButton(no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
			AlertDialog alertDialogEdit = alertDialogBuilderExit.create();
			// show it
			alertDialogEdit.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
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
				return true;
			}
			return false;
		}
	};
	// ContextMenu al dejar pulsado un cursor
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{  
		super.onCreateContextMenu(menu, view, menuInfo);
		menu.setHeaderTitle(R.string.titleQMenu);
		menu.add(0, view.getId(), 0, R.string.see);//groupId, itemId, order, title
		menu.add(0, view.getId(), 0, R.string.edit);
		menu.add(0, view.getId(), 0, R.string.delete);
	}
	// Acciones del contextMenu
	@Override
	public boolean onContextItemSelected(MenuItem item){
		if(item.getTitle()==getResources().getString(R.string.see)){
			Log.i(TAG,item.getTitle().toString());
			Log.d(TAG,Integer.toString(cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID))));
			// Creamos un nuevo Intent
			Intent viewNote = new Intent();
			// Pasa los datos en un Bundle desde MyNotesActivity al siguiente Activity -> ViewActivity
			Bundle bundle = new Bundle();
			bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
			bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
			bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
			bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
			viewNote.putExtras(bundle);
			viewNote.setClass(MyNotesActivity.this,ViewNoteActivity.class); // Siguiente Activity
			startActivity(viewNote); // Comienza la actividad ViewNoteActivity
			finish();
		}
		else if(item.getTitle()==getResources().getString(R.string.edit)){
			Log.i(TAG,item.getTitle().toString());
			Log.d(TAG,Integer.toString(cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID))));
			Intent editNote = new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
			bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
			bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
			bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
			editNote.putExtras(bundle);// Pasamos el bundle a la siguiente actividad
			editNote.setClass(MyNotesActivity.this,EditNoteActivity.class);
			startActivity(editNote); // Comienza la actividad EditNoteActivity
			finish();	// Finaliza la actividad ViewNoteActivity
		}else if(item.getTitle()==getResources().getString(R.string.delete)){
			Log.i(TAG,item.getTitle().toString());
			Log.d(TAG,Integer.toString(cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID))));
			DeleteData delete = new DeleteData();// Tarea asincrona
			Integer[] id = new Integer[1];
			id[0] = cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID));
			delete.execute(id);// pasa el id de la nota a eliminar
			try {
				if(delete.get()){// Devuelve true si se ha eliminado la nota en la tarea asincrona
					// Si eliminamos los datos, comenzamos otro activity MyNotesActivity
					Intent myNotes = new Intent();
					myNotes.setClass(MyNotesActivity.this,MyNotesActivity.class);
					startActivity(myNotes); // Comienza la actividad MyNotesActivity
					finish();// Termina la actividad ViewNoteActivity
				}
			} catch (Exception e) {
				Log.i(TAG,e.toString());
			}
		}
		return true;
	}
	// Tarea asincrona para leer la base de datos
	class ReadData extends AsyncTask<Void, Integer, Cursor> {
		// Llamada al empezar
		Cursor cursorAsyn;
		@Override
		protected Cursor doInBackground(Void... nothing) {
			try {
				// Conectamos con la base de datos para lectura
				db = notebook.getNoteBookData().getDataBase().getReadableDatabase();
				// Obtenemos los datos de la base de datos
				cursorAsyn = db.query(NoteBookData.TABLE_NOTES, null, null, null, null, null,NoteBookData.C_CREATED_AT + " DESC");
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
			if(notebook.getNoteBookData().deleteById(id[0]));// Elimina la nota
				bool = true;
			return bool;
		}
		// Llamada cuando la actividad en background ha terminado
		@Override
		protected void onPostExecute(Boolean result) { //
			// Acción al completar la actualización del estado
			if(result){
				Toast.makeText(MyNotesActivity.this, OkDelete,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(MyNotesActivity.this, ErrorDelete,Toast.LENGTH_LONG).show();
			}			
		}
	}
}


