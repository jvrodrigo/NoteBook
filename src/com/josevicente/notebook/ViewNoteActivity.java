package com.josevicente.notebook;

import java.util.concurrent.ExecutionException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ViewNoteActivity extends BaseActivity implements OnClickListener{
	Button btnEdit,btnDelete,btnPublish;
	Bundle bundle;
	LinearLayout layout;
	TextView titleActivity,txtNote,titleNote,createdAtNote;
	String  noteTitle,deleteNote,OkDelete,ErrorDelete,noSave,yes,no,errorPublishNote,OkPublishNote;
	final static String TAG = ViewNoteActivity.class.getSimpleName();
	@Override
	public void finish(){
		try{
			notebook.getNoteBookData().close();//Cerramos la base de datos;
		}catch(Exception ex){
			//Log.i(TAG,ex.toString());
		}
		super.finish();
	}
	protected void onCreate(Bundle savedInstanceState) {
		try{
			super.onCreate(savedInstanceState);
			Log.i(TAG, "onCreated");
			// Creamos los contenedores para pasarlos al layout viewnote.xml
			setContentView(R.layout.viewnote);
			// Instanciamos el layout
			layout = (LinearLayout)findViewById(R.id.layoutViewNote);
			notebook.setPrefLinearLayout(layout);// Filtra las preferencias
			titleActivity = (TextView) findViewById(R.id.titleActivity);
			titleNote = (TextView) findViewById(R.id.viewTitleNote);
			txtNote = (TextView) findViewById(R.id.viewTextNote);
			createdAtNote = (TextView) findViewById(R.id.viewTextCreatedAt);
			//Recuperamos la informacion pasada en el intent anterior(MyNotesActivity)
			bundle = new Bundle();
			bundle = this.getIntent().getExtras();
			//Construimos los mensajes a mostrar. Variables de aplicacion
			titleNote.setText(bundle.getString(NoteBookData.C_TITLE));
			txtNote.setText(bundle.getString(NoteBookData.C_TEXT));
			txtNote.setMovementMethod(ScrollingMovementMethod.getInstance());// Añadimos el scroll en el textView
			registerForContextMenu(txtNote); // ContextMenu copiar todo el texto
			//txtNote.setTextIsSelectable(true);
			long timestamp = bundle.getLong(NoteBookData.C_CREATED_AT);
			CharSequence realTime = DateUtils.getRelativeTimeSpanString(this,timestamp);
			createdAtNote.setText(getResources().getString(R.string.created)+": "+realTime);
			// Filtramos los TextView por la preferencias
			notebook.setPrefTextView(titleActivity, "titleActivity");
			notebook.setPrefTextView(txtNote,NoteBookData.C_TEXT);
			notebook.setPrefTextView(titleNote,NoteBookData.C_TITLE);
			notebook.setPrefTextView(createdAtNote,NoteBookData.C_CREATED_AT);
			// Variables del AlertDialog
			yes = this.getResources().getString(R.string.yes);
			no = this.getResources().getString(R.string.no);
			deleteNote = this.getResources().getString(R.string.msgDeleteNote) + " " + 
					bundle.getString(NoteBookData.C_TITLE) +"?";
			OkDelete = this.getResources().getString(R.string.OkDelete);
			ErrorDelete = this.getResources().getString(R.string.ErrorDelete);
			errorPublishNote = this.getResources().getString(R.string.errorPublishNote);
			OkPublishNote = this.getResources().getString(R.string.okPublishNote);
			
			btnPublish = (Button)findViewById(R.id.btnPublish);
			btnEdit = (Button)findViewById(R.id.btnEdit);
			btnDelete = (Button)findViewById(R.id.btnDelete);
			
			btnPublish.setOnClickListener(this);
			btnEdit.setOnClickListener(this);
			btnDelete.setOnClickListener(this);
		}catch(Exception ex){
				Log.i(TAG,ex.toString());
		}
	}
	@Override
	// Configuracion del boton atrás
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			startActivity(new Intent(this, MyNotesActivity.class));// De ViewNoteActivity -> MyNotesActivity
			finish();
			return true;
	}
		return super.onKeyDown(keyCode, event);
	}
	// AlertDialog configuration
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btnPublish:
				String [] data = new String[2];
				data[0]=titleNote.getText().toString();
				data[1]=txtNote.getText().toString();
				PublishPost publish = new PublishPost();
				publish.execute(data);
				/*try{
					if(publish.get()){
						
					}
				}catch(ExecutionException e){
					Log.i(TAG,e.getMessage());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			break;
			case R.id.btnEdit:		
				Intent editNote = new Intent();
				editNote.putExtras(bundle);// Pasamos el bundle a la siguiente actividad
				editNote.setClass(ViewNoteActivity.this,EditNoteActivity.class);
				startActivity(editNote); // Comienza la actividad EditNoteActivity
				finish();	// Finaliza la actividad ViewNoteActivity
			break;
			case R.id.btnDelete:
				// Crea un Alert para Eliminar la nota
				AlertDialog.Builder alertDialogBuilderDelete = new AlertDialog.Builder(this);// Alert Delete
				alertDialogBuilderDelete.setTitle(R.string.buttonDelete);
				// set dialog message
				alertDialogBuilderDelete
					.setMessage(deleteNote)
					.setCancelable(false)
					.setPositiveButton(yes,new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog,int id) {
							DeleteData delete = new DeleteData();// Tarea asincrona
							delete.execute();
							try {
								if(delete.get()){// Devuelve true si se ha eliminado la nota en la tarea asincrona
									// Si eliminamos los datos, comenzamos otro activity MyNotesActivity
									Intent myNotes = new Intent();
									myNotes.setClass(ViewNoteActivity.this,MyNotesActivity.class);
									startActivity(myNotes); // Comienza la actividad MyNotesActivity
									finish();// Termina la actividad ViewNoteActivity
								}
							} catch (InterruptedException e) {
								Log.i(TAG,e.toString());
							} catch (ExecutionException e) {
								Log.i(TAG,e.toString());
							}
							
							
						}						
					})
				.setNegativeButton(no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// si este boton is pulsado, se cerrará
						// el dialog box y no hará nada
						dialog.cancel();
					}
				});
				AlertDialog alertDialogDelete = alertDialogBuilderDelete.create();
				// show it
				alertDialogDelete.show();
			break;
			default:break;
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
	// Tarea asincrona para eliminar la nota
	class DeleteData extends AsyncTask<Void, Integer, Boolean> {
		// Llamada al empezar
		Cursor cursorAsyn;
		@Override
		protected Boolean doInBackground(Void... nothing) {
			Boolean bool = false;
			if(notebook.getNoteBookData().deleteById(bundle.getInt(NoteBookData.C_ID)))// Elimina la nota
				bool = true;
			return bool;
		}
		// Llamada cuando hay que actualizar algo en el proceso
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values); // No la usamos
		}
		// Llamada cuando la actividad en background ha terminado
		@Override
		protected void onPostExecute(Boolean result) { //
			// Acción al completar la actualización del estado
			if(result){
				Toast.makeText(ViewNoteActivity.this, OkDelete,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(ViewNoteActivity.this, ErrorDelete,Toast.LENGTH_LONG).show();
			}			
		}
	}
	public class PublishPost extends AsyncTask<String, Integer, Boolean>{
		String NAMESPACE = "urn:GuardarPost";
		//String URL="http://192.168.1.34/SOAP_Servidor/server.php?wsdl"; Para modo local
		String URL="http://josevicente.zz.mu/notebook/server.php?wsdl";
		String METHOD_NAME = "guardarPost";
		String SOAP_ACTION = "urn:GuardarPost#guardarPost";
		@Override
		protected Boolean doInBackground(String... params) {
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			request.addProperty("title", titleNote.getText().toString());
			request.addProperty("text", txtNote.getText().toString());
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			HttpTransportSE transporte = new HttpTransportSE(URL);
			try{
			    transporte.call(SOAP_ACTION, envelope);
			 
			    //Se procesa el resultado devuelto
			    //...
			    return true;
			}catch (Exception e){
			    Log.i(TAG,"ERROR Publish->"+e.getMessage());
			    return false;
			}
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values); // No la usamos
		}
		// Llamada cuando la actividad en background ha terminado
		@Override
		protected void onPostExecute(Boolean result) { //
			// Acción al completar el guardado de la nota
			if(result){
				Toast.makeText(ViewNoteActivity.this, OkPublishNote,Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(ViewNoteActivity.this, errorPublishNote,Toast.LENGTH_LONG).show();
				
			}
		}
		
	}
}