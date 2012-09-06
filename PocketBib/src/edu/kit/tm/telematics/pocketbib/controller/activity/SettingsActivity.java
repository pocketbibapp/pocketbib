package edu.kit.tm.telematics.pocketbib.controller.activity;

import static edu.kit.tm.telematics.pocketbib.model.Constants.KEY_CACHE_QUALITY;
import static edu.kit.tm.telematics.pocketbib.model.Constants.KEY_LIBRARY_SERVER;
import static edu.kit.tm.telematics.pocketbib.model.Constants.KEY_LIBRARY_SERVER_PORT;
import static edu.kit.tm.telematics.pocketbib.model.Constants.KEY_LIBRARY_SERVER_SSL;
import static edu.kit.tm.telematics.pocketbib.model.Constants.PORT_HTTP;
import static edu.kit.tm.telematics.pocketbib.model.Constants.PORT_HTTPS;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.impl.edu.kit.RestClient;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

	private final static String TAG = "SettingsActivity";
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.myapppreferences);

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		EditTextPreference pref = (EditTextPreference) findPreference(KEY_LIBRARY_SERVER_PORT);
		pref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}

	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			p.setSummary(listPref.getEntry());
		}
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			p.setSummary(editTextPref.getText());
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePrefSummary(findPreference(key));

		if(key.equals(KEY_LIBRARY_SERVER_SSL)) {
			boolean sslEnabled = sharedPreferences.getBoolean(KEY_LIBRARY_SERVER_SSL, true);	
			String currentPort = sharedPreferences.getString(KEY_LIBRARY_SERVER_PORT, PORT_HTTP);
			String newPort = currentPort;
			
			if(sslEnabled && currentPort.equals(PORT_HTTP)) {
				newPort = PORT_HTTPS;
			} else if(!sslEnabled && currentPort.equals(PORT_HTTPS)) {
				newPort = PORT_HTTP;
			}
			
			sharedPreferences.edit().putString(KEY_LIBRARY_SERVER_PORT, newPort).commit();
			findPreference(KEY_LIBRARY_SERVER_PORT).setSummary(newPort);
		}
		
		if (key.equals(KEY_CACHE_QUALITY)) {
			PocketBibApp.getItemCoverManager().getLocalCoverCache().clearCache();
		} else if (key.equals(KEY_LIBRARY_SERVER) 
				|| key.equals(KEY_LIBRARY_SERVER_PORT)
				|| key.equals(KEY_LIBRARY_SERVER_SSL)) {

			RestClient.getInstance().initializeConnection();
		}
	}
}
