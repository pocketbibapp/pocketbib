package edu.kit.tm.telematics.pocketbib.controller.activity;

import java.util.Locale;

import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;

public class HelpActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		setDisplayShowUp(WelcomeActivity.class, true);

		// loads a different html file with the FAQ depending on the device language
		WebView helpText = (WebView) findViewById(R.id.help_text);
		if (Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage())) {
			helpText.loadUrl("file:///android_asset/faq-de.html");
		} else {
			helpText.loadUrl("file:///android_asset/faq-en.html");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.help, menu);

		menu.findItem(R.id.item_my_account).setVisible(false);
		menu.findItem(R.id.item_administration).setVisible(false);
		menu.findItem(R.id.item_help).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_about_us:
			// shows the licensing information
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.dialog_title_about_us).setMessage(R.string.dialog_message_about_us)
					.setNeutralButton(android.R.string.ok, null).show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
