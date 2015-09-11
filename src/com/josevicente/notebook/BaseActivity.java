package com.josevicente.notebook;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class BaseActivity extends Activity {
	static NoteBookApplication notebook;
	TextView textView;
	private static final String TAG = BaseActivity.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreated");
		// Objeto aplicacion para compartir las preferencias
		notebook = (NoteBookApplication) getApplication();
		///Log.i(TAG, Boolean.toString(notebook.getPrefs().getBoolean("autoSaveService", true)));
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) { //Cuando pulsa un item de preferencias
			case R.id.itemDrafts:
				startActivity(new Intent(this, ListDraftsActivity.class));
				finish();
				break;
			case R.id.textMyNotes:
				startActivity(new Intent(this, MyNotesActivity.class));
				finish();
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				finish();
				break;
			case R.id.itemNewNote:
				startActivity(new Intent(this, NewNoteActivity.class));
				finish();
				return true;			
		}
		return true;
	}// Fin del menu de preferencias
	public boolean onMenuOpened(int featureId, Menu menu) {
		return true;
	}

}