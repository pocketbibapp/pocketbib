package edu.kit.tm.telematics.pocketbib.controller.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;

/**
 * This activity displays a login form.
 */
public class LoginActivity extends BaseActivity {

	/** EditText containing the e-mail address */
	private EditText emailEditText;

	/** EditText containing the password */
	private EditText passwordEditText;

	/**
	 * Toggle (Switch or CheckBox) with an option to save the password
	 * permanently
	 */
	private CompoundButton saveLoginToggle;

	/** the Button to initiate a login */
	private Button loginButton;

	/** TextView containing an optional login error message */
	private TextView errorTextView;
	
	/** ProgressBar, displayed during a login attempt */
	private ProgressBar progressBar;
	
	private ScrollView scrollView;
	
	/** Tag for logging purposes */
	private final static String TAG = "LoginActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Don't display this activity if the user is already logged in.
		if (User.getCurrentUser() instanceof LoggedInUser) {
			Log.w(TAG, "Cannot access LoginActivity when already logged in. Navigating to WelcomeActivity.");
			startActivity(new Intent(this, WelcomeActivity.class));
			finish();
		}

		setContentView(R.layout.activity_login);
		setDisplayShowUp(WelcomeActivity.class, true);

		emailEditText = (EditText) findViewById(R.id.input_email);
		passwordEditText = (EditText) findViewById(R.id.input_password);
		saveLoginToggle = (CompoundButton) findViewById(R.id.toggle_save_password);
		loginButton = (Button) findViewById(R.id.button_login);
		errorTextView = (TextView) findViewById(R.id.label_error);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		scrollView = (ScrollView) findViewById(R.id.scroll);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		setLoginLogoutMenuItemVisible(menu, false);
		menu.findItem(R.id.item_search).setVisible(false);
		
		getSupportMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_forgot_password:
			showDialog(R.string.dialog_title_forgot_password, R.string.dialog_message_forgot_password);
			return true;

		case R.id.item_register:
			showDialog(R.string.dialog_title_register, R.string.dialog_message_register);
			return true;

		case R.id.item_options:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Shows a simple alert dialog, with an "OK"-button.
	 * 
	 * @param titleRes
	 *            the title resource
	 * @param messageRes
	 *            the message resource
	 */
	protected void showDialog(int titleRes, int messageRes) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(titleRes);
		dialog.setMessage(messageRes);
		dialog.setNeutralButton(android.R.string.ok, null);
		dialog.show();
	}

	/**
	 * Called when the user clicks on "Login".
	 * 
	 * @param view
	 *            the source View (unused, may be null)
	 */
	public void onLoginClicked(View v) {
		String email = emailEditText.getText().toString().trim();
		String password = passwordEditText.getText().toString();
		
		if(email.length() == 0) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.dialog_title_no_user_account);
			dialog.setMessage(R.string.dialog_message_no_user_account);
			dialog.setNegativeButton(R.string.dialog_action_no_user_account_no, null);
			dialog.setPositiveButton(R.string.dialog_action_no_user_account_yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showDialog(R.string.dialog_title_register, R.string.dialog_message_register);
				}
			});
			dialog.show();
		} else if(password.length() == 0) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.dialog_title_enter_password);
			dialog.setMessage(R.string.dialog_message_enter_password);
			dialog.setPositiveButton(android.R.string.ok, null);
			dialog.setNegativeButton(R.string.dialog_action_enter_password_forgot, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showDialog(R.string.dialog_title_forgot_password, R.string.dialog_message_forgot_password);
				}
			});
			AlertDialog alertDialog = dialog.create();
			alertDialog.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					passwordEditText.requestFocus();
				}
			});
			alertDialog.show();
		} else {
			// don't execute the login server request on the main ui thread - this
			// will cause a bad behaviour. Do it on a background thread instead!
			
			new LoginTask().execute();
		}
		
		
	}

	/**
	 * A task to try a login asynchronously.
	 */
	protected class LoginTask extends AsyncTask<Void, Void, ResponseCode> {

		@Override
		protected void onPreExecute() {
			// Prevent two parallel requests, by disabling the login button
			// during an active request
			enableInputs(false);
			loginButton.setText(R.string.label_login_in_progress);
			progressBar.setVisibility(View.VISIBLE);
			progressBar.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in_from_right));
			errorTextView.setVisibility(View.GONE);
		}
		
		private void enableInputs(boolean enabled) {
			loginButton.setEnabled(enabled);
			emailEditText.setEnabled(enabled);
			passwordEditText.setEnabled(enabled);
			saveLoginToggle.setEnabled(enabled);
		}
		
		@Override
		protected ResponseCode doInBackground(Void... params) {
			String email = emailEditText.getText().toString().trim();
			String password = passwordEditText.getText().toString();

			ResponseCode responseCode = PocketBibApp.getRegistrationProvider().login(email, password,
					saveLoginToggle.isChecked());
			
			//Log.e("TAG", "Login responseCode: " + String.valueOf(responseCode));
			return responseCode;
		}

		@Override
		protected void onCancelled() {
			progressBar.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_out_to_right));
			progressBar.setVisibility(View.GONE);
			loginButton.setText(R.string.label_login);
			enableInputs(true);
		}

		@Override
		protected void onPostExecute(ResponseCode responseCode) {
			if (responseCode == ResponseCode.OK) {
				// Login correct, show WelcomeActivity
				Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {
				// Login failed
				progressBar.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_out_to_right));
				progressBar.setVisibility(View.GONE);
				
				loginButton.setText(R.string.label_login);
				enableInputs(true);

				errorTextView.setText(responseCode.getErrorString());
				errorTextView.setVisibility(View.VISIBLE);
				errorTextView.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in_from_right));
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		}
	}
}
