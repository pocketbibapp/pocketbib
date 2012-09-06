package edu.kit.tm.telematics.pocketbib.controller.activity;

import java.util.List;

import junit.framework.Assert;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.controller.activity.admin.AdminActivity;
import edu.kit.tm.telematics.pocketbib.controller.activity.admin.UserAddEditActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;

public abstract class BaseActivity extends SherlockFragmentActivity {

	private final static Handler UI_HANDLER = new Handler(Looper.getMainLooper());

	private Class<? extends Activity> upActivity;

	private boolean isErrorLayoutInitialized = false;

	private View errorContainer = null;

	private ViewGroup errorLayout = null;

	private TextView errorSmileyTextView = null;

	private TextView errorTextView = null;

	private Button reloadButton = null;

	private ProgressBar reloadProgressBar = null;

	private int errorCount = 0;

	private String[] errorSmilies = null;

	private ReloadRunnable reloadRunnable = null;

	private final static String TAG = "BaseActivity";

	@Override
	protected void onResume() {
		super.onResume();

		// check if the current user account is activated
		if (!User.getCurrentUser().isActive()) {
			Toast.makeText(this, getString(R.string.message_user_invalid), Toast.LENGTH_LONG).show();
			onLogoutClick(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.base, menu);

		setLoginLogoutMenuItemVisible(menu, true);

		if (User.getCurrentUser().isAdministrator()) {
			menu.findItem(R.id.item_administration).setVisible(true);
		}
		if (User.getCurrentUser() instanceof LoggedInUser) {
			menu.findItem(R.id.item_my_account).setVisible(true);
		}

		// Disable button if no recognition service is present
		List<ResolveInfo> activities = getPackageManager().queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			menu.findItem(R.id.item_search_voice).setVisible(false);
		}

		return true;
	}

	public void setLoginLogoutMenuItemVisible(Menu menu, boolean visible) {
		MenuItem loginItem = menu.findItem(R.id.item_login);
		MenuItem logoutItem = menu.findItem(R.id.item_logout);

		if (User.getCurrentUser() instanceof LoggedInUser) {
			loginItem.setVisible(false);
			logoutItem.setVisible(visible);
		} else {
			logoutItem.setVisible(false);
			loginItem.setVisible(visible);
		}
	}

	protected void setDisplayShowUp(Class<? extends Activity> upActivity, boolean show) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (show) {
			this.upActivity = upActivity;
		}
	}	

	/**
	 * Initializes the error layout manually. This function requires a inflated
	 * {@code show_error.xml} layout.
	 * 
	 * @param errorLayout
	 *            the error layout (see {@code show_error.xml}
	 */
	public void initializeErrorLayout(View errorContainer) {
		Assert.assertNotNull(errorContainer);

		this.errorContainer = errorContainer;
		this.errorLayout = (ViewGroup) errorContainer.findViewById(R.id.layout_error);

		errorSmileyTextView = (TextView) errorLayout.findViewById(R.id.label_error_smiley);
		errorTextView = (TextView) errorLayout.findViewById(R.id.label_error_message);
		reloadButton = (Button) errorLayout.findViewById(R.id.button_error_reload);
		reloadProgressBar = (ProgressBar) errorLayout.findViewById(R.id.progress_error_reload);

		if (errorSmileyTextView == null || errorTextView == null || reloadButton == null || reloadProgressBar == null) {
			throw new AssertionError("Couldn't find error related views in the XML. "
					+ "Did you forget to <include> show_error.xml in your layout file?");
		}

		errorSmilies = getResources().getStringArray(R.array.error_smilies);
		Assert.assertNotNull(errorSmilies);

		isErrorLayoutInitialized = true;
	}

	/**
	 * Calling this method requires either a included {@code show_error.xml}
	 * layout or a previous call to {@link #initializeErrorLayout(ViewGroup)}.<br>
	 * Makes the error layout visible and displays the supplied error String.
	 * Calling this function does NOT display a reload button.
	 * 
	 * @param errorString
	 *            the error String
	 */
	public void displayError(CharSequence errorString) {
		if (!isErrorLayoutInitialized) {
			initializeErrorLayout(findViewById(R.id.error_container));
		}

		Log.e(TAG, "Displaying error: " + errorString + ")");

		int smileyIndex = errorCount / 5 % errorSmilies.length;

		errorCount++;

		if (errorContainer != null) {
			errorContainer.setVisibility(View.VISIBLE);
			errorSmileyTextView.setText(errorSmilies[smileyIndex]);
			errorTextView.setText(errorString);
			reloadProgressBar.setVisibility(View.GONE);

			if (reloadRunnable != null) {
				reloadButton.setText(R.string.reload);
				reloadButton.setEnabled(true);
				reloadButton.setVisibility(View.VISIBLE);
			} else {
				reloadButton.setEnabled(false);
				reloadButton.setVisibility(View.GONE);
			}
		} else {
			Log.e(TAG, "errorContainer is null!");
			Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Calling this method requires either a included {@code show_error.xml}
	 * layout or a previous call to {@link #initializeErrorLayout(ViewGroup)}.<br>
	 * Makes the error layout visible and displays the supplied error String.
	 * This function also makes a reload button visible and executes the given
	 * ReloadRunnable when the user clicks on the reload button.
	 * 
	 * @param errorString
	 *            the error String
	 * @param reloadAction
	 *            the ReloadRunnable
	 */
	public void displayError(CharSequence errorString, ReloadRunnable reloadAction) {
		reloadRunnable = reloadAction;
		displayError(errorString);
	}

	public void onErrorReloadClick(View source) {
		if (reloadRunnable == null) {
			Log.e(TAG, "Cannot reload - no reload runnable was passed to displayError()!");
			return;
		}

		new ExecuteReloadRunnableTask().execute();
	}

	public void hideError() {
		if (errorContainer != null)
			errorContainer.setVisibility(View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent searchIntent;

		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, upActivity);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;

		case R.id.item_login:
			startActivity(new Intent(this, LoginActivity.class));
			return true;
		case R.id.item_logout:
			onLogoutClick(null);
			return true;
		case R.id.item_search:
			// search menu is being displayed via XML
			return true;
		case R.id.item_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		case R.id.item_administration:
			startActivity(new Intent(this, AdminActivity.class));
			return true;

		case R.id.item_my_account:
			startActivity(new Intent(this, UserAddEditActivity.class).putExtra(Constants.Intent.KEY_USER_PARCEL,
					(LoggedInUser) User.getCurrentUser()));
			return true;

		case R.id.item_search_text:
			searchIntent = new Intent(this, SearchResultsActivity.class);
			searchIntent.putExtra(Constants.KEY_QUERY_TYPE, Constants.VALUE_QUERY_TYPE_SIMPLE);
			startActivity(searchIntent);
			return true;

		case R.id.item_search_voice:
			searchIntent = new Intent(this, SearchResultsActivity.class);
			searchIntent.putExtra(Constants.KEY_QUERY_TYPE, Constants.VALUE_QUERY_TYPE_VOICE);
			startActivity(searchIntent);
			return true;

		case R.id.item_search_extended:
			startActivity(new Intent(BaseActivity.this, ExtendedSearchFormActivity.class));
			return true;

		case R.id.item_search_scan_barcode:
			searchIntent = new Intent(this, SearchResultsActivity.class);
			searchIntent.putExtra(Constants.KEY_QUERY_TYPE, Constants.VALUE_QUERY_TYPE_SCAN_BARCODE);
			startActivity(searchIntent);
			return true;

		default:
			return false;
		}
	}

	public void onLogoutClick(View source) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_logout);
		dialog.setMessage(R.string.dialog_message_logout);
		dialog.setPositiveButton(R.string.dialog_action_logout, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PocketBibApp.getRegistrationProvider().logout();
				startActivity(new Intent(BaseActivity.this, WelcomeActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				finish();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.show();
	}

	/**
	 * A task that handles the reloading and shows/hides the corresponding
	 * views.
	 * 
	 */
	private class ExecuteReloadRunnableTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onCancelled(Boolean executeSuccessful) {
			onPostExecute(executeSuccessful);
		}

		@Override
		protected void onPostExecute(Boolean executeSuccessful) {
			reloadProgressBar.setVisibility(View.GONE);
			reloadButton.setVisibility(View.VISIBLE);

			if (executeSuccessful) {
				errorLayout.setVisibility(View.GONE);
			}
		}

		@Override
		protected void onPreExecute() {
			reloadButton.setVisibility(View.GONE);
			reloadProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return reloadRunnable.execute();
		}
	}

	/**
	 * A Runnable, which returns true on success or false on error.
	 */
	public static abstract class ReloadRunnable implements Runnable {

		@Override
		public final void run() {
			execute();
		}

		/**
		 * Executes the Runnable. Insert your code in this method. If you got
		 * suitable data, return true. If the error shall be shown again, return
		 * false and call displayError.
		 * 
		 * @return true, if the action was completed successfully and the error
		 *         shall be hidden. Returns false if an error has to be
		 *         displayed.
		 */
		public abstract boolean execute();

	}

	/**
	 * A reload task. Use ReloadTask instead of ReloadRunnable, if you wish to
	 * execute any operation (e.g. setListAdapter()) after data was loaded on
	 * the background thread.
	 * 
	 * @param <T>
	 *            the type of data (e.g. UserAdapter, LoggedInUser, Item, ...)
	 */
	public static abstract class ReloadTask<T> extends ReloadRunnable {

		public void onPreExecute() {
			
		}
		
		/**
		 * Override this method to perform a computation on a background thread.
		 * 
		 * @return the response. The error layout will be hidden if the
		 *         ResponseCode is OK. If the ResponseCode is anything else,
		 *         call displayError
		 */
		public abstract Response<T> doInBackground();

		/**
		 * Runs on the UI thread after {@link #doInBackground()}.
		 * 
		 * @param response
		 *            the Response of {@link #doInBackground()}
		 */
		public void onPostExecute(Response<T> response) {
		}

		public final boolean execute() {
			UI_HANDLER.post(new Runnable() {
				@Override
				public void run() {
					onPreExecute();
				}
			});
			
			final Response<T> response = doInBackground();

			UI_HANDLER.post(new Runnable() {
				@Override
				public void run() {
					onPostExecute(response);
				}
			});

			return response.getResponseCode() == ResponseCode.OK;
		}

	}

}
