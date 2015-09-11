package com.josevicente.notebook;

import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ViewDraftActivity extends BaseActivity implements OnClickListener{
	Button btnDelete,btnSave;
	Bundle bundle;
	Cursor cursor;
	LinearLayout layout;
	SQLiteDatabase db;
	TextView titleActivity,txtNote,titleNote,createdAtNote;
	EditText input;
	String yes,no,Ok;
	String  titleDraft,saveDraft,deleteDraft,okDeleteDraft,errorDeleteDraft,errorSaveDraft;
	final static String TAG = ViewDraftActivity.class.getSimpleName();
	@Override
	public void finish(){
		try{
			notebook.getNoteBookData().close();//Cerramos la base de datos;
		}catch(Exception e){
			Log.i(TAG,e.toString());
		}
		super.finish();
	}
	protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Log.i(TAG, "onCreated");
			// Creamos los contenedores para pasarlos al layout viewnote.xml
			setContentView(R.layout.viewdraft);
			// Instanciamos el layout
			layout = (LinearLayout)findViewById(R.id.layoutViewDraft);
			notebook.setPrefLinearLayout(layout);// Filtra las preferencias
			titleActivity = (TextView) findViewById(R.id.titleActivity);
			titleNote = (TextView) findViewById(R.id.viewTitleDraft);
			txtNote = (TextView) findViewById(R.id.viewTextDraft);
			createdAtNote = (TextView) findViewById(R.id.viewDraftCreatedAt);
			//Recuperamos la informacion pasada en el intent
			bundle = new Bundle();
			bundle = this.getIntent().getExtras();
			//Construimos el mensaje a mostrar. Variables de aplicacion
			titleNote.setText(bundle.getString(NoteBookData.C_TITLE));
			txtNote.setText(bundle.getString(NoteBookData.C_TEXT));
			txtNote.setMovementMethod(ScrollingMovementMethod.getInstance());// Añadimos el scroll en el textView
			registerForContextMenu(txtNote);// ContextMenu copiar todo el texto
			long timestamp = bundle.getLong(NoteBookData.C_CREATED_AT);
			CharSequence realTime = DateUtils.getRelativeTimeSpanString(this,timestamp);
			createdAtNote.setText(getResources().getString(R.string.created)+": "+realTime);
			// Filtramos los TextView por la preferencias
			notebook.setPrefTextView(titleActivity, "titleActivity");
			notebook.setPrefTextView(txtNote,NoteBookData.C_TEXT);
			notebook.setPrefTextView(titleNote,NoteBookData.C_TITLE);
			notebook.setPrefTextView(createdAtNote,NoteBookData.C_CREATED_AT);
			// Variables del AlertDialog
			titleDraft = this.getResources().getString(R.string.titleDrafts);
			saveDraft = this.getResources().getString(R.string.saveDraft);
			deleteDraft = this.getResources().getString(R.string.msgDeleteDraft) + " " + 
					bundle.getString(NoteBookData.C_TITLE) +"?";
			okDeleteDraft = this.getResources().getString(R.string.okDeleteDraft);
			errorDeleteDraft = this.getResources().getString(R.string.errorDeleteDraft);
			errorSaveDraft = this.getResources().getString(R.string.errorSaveDraft);
			yes = this.getResources().getString(R.string.yes);
			no = this.getResources().getString(R.string.no);
			Ok = this.getResources().getString(R.string.saveOk);
			btnDelete = (Button)findViewById(R.id.btnDeleteDraft);
			btnSave = (Button)findViewById(R.id.btnSaveDraft);
			btnDelete.setOnClickListener(this);
			btnSave.setOnClickListener(this);
	
	}
	@Override
	// Configuracion del boton atrás
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			startActivity(new Intent(this, ListDraftsActivity.class));// Vuelve a listar los borradores
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btnDeleteDraft:
				if(notebook.getNoteBookData().deleteDraftById(bundle.getInt(NoteBookData.C_ID))){
					Toast.makeText(ViewDraftActivity.this, okDeleteDraft,Toast.LENGTH_LONG).show();
					// Si eliminamos los datos, comenzamos otro activity ListDraftActivity
					Intent listDrafts = new Intent();
					listDrafts.setClass(ViewDraftActivity.this,ListDraftsActivity.class);
					startActivity(listDrafts); // Comienza la actividad ListDraftActivity
					finish();// Termina la actividad ViewNote
				}else{
					Toast.makeText(ViewDraftActivity.this, errorDeleteDraft,Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.btnSaveDraft:
				AlertDialog.Builder alertDialogBuilderSave = new AlertDialog.Builder(this);
				// Inflamos el layout para introducir otro layout del titulo de la nota
				LayoutInflater layoutInflater = LayoutInflater.from(this);
				View promptView = layoutInflater.inflate(R.layout.prompt, null);// Prompt para introducir el título
				// set title
				input = (EditText) promptView.findViewById(R.id.titleInput); // Variable que almacenará el titulo
				// Crea un alert al ser pulsado para guardar la nota
				alertDialogBuilderSave.setView(promptView);
				alertDialogBuilderSave.setTitle(titleDraft);
				// set dialog message
				alertDialogBuilderSave
					.setMessage(saveDraft)
					.setCancelable(false)
					.setPositiveButton(yes,new DialogInterface.OnClickListener() {// Si pulsa Si
						public void onClick(DialogInterface dialog,int id) {
						if(input.getText().length()==0){ // Si no ha introducido ninguna letra en el titulo
							Toast.makeText(ViewDraftActivity.this, getResources().getString(R.string.toastInputTitle),Toast.LENGTH_LONG).show();
						}else{
							try{
								// Tarea asincrona SaveData();						 
								SaveData save = new SaveData();
								save.execute();
								cursor = save.get();// Obtiene el cursor de la tarea Asincrona
								cursor.moveToFirst();
								// Creamos un nuevo Intent para ver el borrador guardado en ViewNoteActivity
								Intent viewNote = new Intent();
								// Pasa los datos de la Nota guardada al siguiente activity -> View
								Bundle bundle = new Bundle();
								bundle.putInt(NoteBookData.C_ID,cursor.getInt(cursor.getColumnIndex(NoteBookData.C_ID)));
								bundle.putString(NoteBookData.C_TITLE,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TITLE)));
								bundle.putString(NoteBookData.C_TEXT,cursor.getString(cursor.getColumnIndex(NoteBookData.C_TEXT)));
								bundle.putLong(NoteBookData.C_CREATED_AT,cursor.getLong(cursor.getColumnIndex(NoteBookData.C_CREATED_AT)));
								viewNote.putExtras(bundle);
								viewNote.setClass(ViewDraftActivity.this,ViewNoteActivity.class);
								startActivity(viewNote); // Comienza la actividad ViewNoteActivity para ver la nota
								finish(); // Termina la actividad ViewDraftActivity
							}catch(Exception ex){
								Log.i(TAG,"Error saving data");
							}		
						}
						}
					}).setNegativeButton(no,new DialogInterface.OnClickListener() { // Si pulsa no
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							dialog.cancel();
						}
					});
				AlertDialog alertDialogDelete = alertDialogBuilderSave.create();
				// show it
				alertDialogDelete.show();
				break;
		}
	}
	// Context Menu para copiar el texto de la nota en el servicio CLIPBOARD
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.selectAll);
        menu.add(0, 0, 0, R.string.menuCopyToClipBoard);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setText(txtNote.getText());
		return true;
	}
	class SaveData extends AsyncTask<Void, Integer, Cursor> {
		// Llamada al empezar
		Cursor cursorAsyn;
		@Override
		protected Cursor doInBackground(Void... nothing) {
			try {
				// Gets the data repository in write mode
				db = notebook.getNoteBookData().getDataBase().getWritableDatabase();
				Date date = new Date();
				// Create a new map of values, where column names are the keys
				ContentValues values = new ContentValues();
				// No hace falta incluir el ID porque está configurado como integer autoincrement
				values.put(NoteBookData.C_TITLE, input.getText().toString());
				values.put(NoteBookData.C_TEXT, txtNote.getText().toString());
				values.put(NoteBookData.C_CREATED_AT, date.getTime());
				// Insert the new row, returning the primary key value of the new row
				//guarda en la base de datos
				long newId = db.insert(NoteBookData.TABLE_NOTES,null,values);
				// Eliminamos el borrador de la base de datos-> borradores
				notebook.getNoteBookData().deleteDraftById(bundle.getInt(NoteBookData.C_ID));
				// Busca en la base de datos la ultima nota introducida para comprobar que es correcto y pasar el bundle a otra actividad
				String query = "SELECT * FROM "+NoteBookData.TABLE_NOTES +" WHERE _id = "+newId;
				cursorAsyn = db.rawQuery(query,null);// Pasa los datos de la consulta al cursor
				return cursorAsyn;
			} catch (Exception e) {
				Toast.makeText(ViewDraftActivity.this, errorSaveDraft,Toast.LENGTH_LONG).show();
				Log.i(TAG,e.toString());
				return cursorAsyn = null;
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
			// Acción al completar el guardado de la nota
			if(result==null){
				Toast.makeText(ViewDraftActivity.this, errorSaveDraft,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(ViewDraftActivity.this, Ok,Toast.LENGTH_LONG).show();
			}
		}
	}
}
