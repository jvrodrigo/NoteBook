package com.josevicente.notebook;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
// NoteBookApplication es la aplicación registra las preferencias de la aplicación
// para poder acceder a las preferencias en toda la aplicación
// Tambien se incluye la base de datos para poder acceder a ella desde cualquier actividad de la aplicación
public class NoteBookApplication extends Application implements OnSharedPreferenceChangeListener{
	private static final String TAG = NoteBookApplication.class.getSimpleName();
	private SharedPreferences prefs;
	private NoteBookData noteBookData;
	public EditText txtNote;
	public boolean flagSave=false; // Bandera para eliminar el borrador si la nota se ha guardado
	public boolean getFlagSave(){
		return flagSave;
	}
	public void setFlagSave(boolean flag){
		flagSave = flag;
	}
	// Devuelve las preferencias compartidas
	public SharedPreferences getPrefs(){
		return prefs;
	}
	public NoteBookData getNoteBookData(){//Base de datos de la aplicacion, devuelve la vase de datos
		if(noteBookData==null){
			noteBookData = new NoteBookData(this);
		}
		return noteBookData;				
	}
	private boolean autoSaveServiceRunning;
	public boolean isServiceRunning() {
		return autoSaveServiceRunning;
	}
	public void setServiceRunning(boolean autoSaveServiceRunning) {
		this.autoSaveServiceRunning = autoSaveServiceRunning;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreated");
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}
	public synchronized void setPrefLinearLayout(LinearLayout layout){
		// Color del fondo
		layout.setBackgroundColor(Color.parseColor(prefs.getString("backgroundColor", "WHITE")));
	}
	// Esta función pasa los TextView de la aplicación por la preferencias de texto y color
	public synchronized void setPrefTextView(TextView v,String key) {
		try{
			// Color de la fuente
			v.setTextColor(Color.parseColor(prefs.getString("fontColor", "BLACK")));
			// Color del fondo
			v.setBackgroundColor(Color.parseColor(prefs.getString("backgroundColor", "WHITE")));
			if(key.equals(NoteBookData.C_TITLE)){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()+2);// Tamaño del texto
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.BOLD));//Estilo de la fuente
			}else if(key.equals(NoteBookData.C_TEXT)){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue());
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}else if(key.equals(NoteBookData.C_CREATED_AT)){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()-4);
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}else if(key.equals("textCount")){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()-4);
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}else if(key.equals("titleActivity")){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()+8);
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}
		}catch(Exception ex){
			Log.i(TAG,ex.getStackTrace().toString());
		}
	}
	//Esta función pasa los EditText de la aplicación por la preferencias de texto y color
	public synchronized void setPrefEditText(EditText v,String key) {
		try{
			// Color de la fuente
			v.setTextColor(Color.parseColor(prefs.getString("fontColor", "BLACK")));
			// Color del fondo
			v.setBackgroundColor(Color.parseColor(prefs.getString("backgroundColor", "WHITE")));
			if(key.equals(NoteBookData.C_TITLE)){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()+2);// Tamaño del texto
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.BOLD));//Estilo de la fuente
			}else if(key.equals(NoteBookData.C_TEXT)){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue());
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}else if(key.equals(NoteBookData.C_CREATED_AT)){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()-4);
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}else if(key.equals("textCount")){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()-4);
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}else if(key.equals("titleActivity")){
				v.setTextSize(Integer.valueOf(prefs.getString("fontSize","16")).floatValue()+4);
				v.setTypeface(Typeface.create(prefs.getString("fontStyle", "default"),Typeface.NORMAL));
			}
		}catch(Exception ex){
			Log.i(TAG,ex.getStackTrace().toString());
		}
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		
	}
}
