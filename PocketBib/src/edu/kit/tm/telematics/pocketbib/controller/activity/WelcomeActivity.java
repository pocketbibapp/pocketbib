package edu.kit.tm.telematics.pocketbib.controller.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.BibtexUtil;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;

public class WelcomeActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

	private LoggedInUser currentUser;
	private TextView loginInformationTextView;
	private View noItemsLayout;
	private ListView borrowedItemListView;
	private ItemAdapter adapter;
	private ActionMode mActionMode = null;
	private final static int DIALOG_RETURN = 1;

	private final static String TAG = "WelcomeActivity";

	private ReloadTask<ItemAdapter> reloadTask = new ReloadTask<ItemAdapter>() {
		@Override
		public Response<ItemAdapter> doInBackground() {
			return PocketBibApp.getLibraryManager().getBorrowedItems(currentUser);
		}

		@Override
		public void onPostExecute(Response<ItemAdapter> response) {
			if (response.getResponseCode() == ResponseCode.OK) {
				hideError();
				adapter = response.getData();
				borrowedItemListView.setAdapter(adapter);
			} else {
				displayError(response.getResponseCode().getErrorString(), this);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (PocketBibApp.getSharedPreferences().getString(Constants.KEY_AUTH_KEY, null) != null) {
			Log.i("WelcomeActivity", "Load saved User");
			PocketBibApp.getRegistrationProvider().loadSavedUser();
		}

		// currentUser is null if not instance of LoggedInUser
		if (User.getCurrentUser() instanceof LoggedInUser) {
			currentUser = (LoggedInUser) User.getCurrentUser();
			// adds a indeterminate progress bar and sets the visibility to
			// false
			// this is used to show that this activity is trying to get
			// information from the internet
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setSupportProgressBarIndeterminateVisibility(false);
		} else {
			currentUser = null;
		}

		// noItemsTextView = (TextView) findViewById(R.id.label_no_items);
		// set views for the LoggedInUser and User
		if (currentUser == null) {
			setContentView(R.layout.activity_welcome_guest);
			// elements for the layout of just a normal User
			loginInformationTextView = (TextView) findViewById(R.id.label_welcome_guest);

		} else {
			setContentView(R.layout.activity_welcome);
			// elements of the layout for a LoggedInUser
			loginInformationTextView = (TextView) findViewById(R.id.label_welcome);
			loginInformationTextView.setText(getResources().getString(R.id.label_welcome, "", ""));

			borrowedItemListView = (ListView) findViewById(android.R.id.list);
			noItemsLayout = findViewById(R.id.layout_no_items);
			borrowedItemListView.setEmptyView(noItemsLayout);
			borrowedItemListView.setOnItemClickListener(this);

			LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this,
					R.anim.list_layout_welcome_activity);
			borrowedItemListView.setLayoutAnimation(animationController);
			borrowedItemListView.setOnItemLongClickListener(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// re-loads and re-sets the current user
		if (User.getCurrentUser() instanceof LoggedInUser) {
			currentUser = (LoggedInUser) User.getCurrentUser();
			loginInformationTextView.setText(getResources().getString(R.string.label_welcome,
					currentUser.getFirstName(), currentUser.getLastName()));

			new SetViewTask().execute();
		} else {
			loginInformationTextView.setText(R.string.label_welcome_guest);
			currentUser = null;
		}

		// hide contextualActionBarSherlock
		if (mActionMode != null)
			mActionMode.finish();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.findItem(R.id.item_search).setVisible(true);
		menu.findItem(R.id.item_options).setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_options:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Handles clicks on the login button
	 * 
	 * @param view
	 *            view
	 */
	public void onLoginClick(View view) {
		startActivity(new Intent(this, LoginActivity.class));
	}

	/**
	 * Handles the clicks on the reload button
	 * 
	 * @param v
	 *            the view that is calling
	 */
	public void onReloadClick(View v) {
		new SetViewTask().execute();
	}

	protected class SetViewTask extends AsyncTask<Void, Void, Response<ItemAdapter>> {

		@Override
		protected void onPreExecute() {
			// shows that the activity is working in the background
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Response<ItemAdapter> doInBackground(Void... params) {
			// is also checked before in onResume() so currentUser should be a
			// LoggedInUser
			if (currentUser != null) {
				Response<ItemAdapter> response = PocketBibApp.getLibraryManager().getBorrowedItems(currentUser);
				adapter = response.getData();
				return response;
			} else {
				return new Response<ItemAdapter>(ResponseCode.ERROR_UNKNOWN, null);
			}

		}

		@Override
		protected void onCancelled() {
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPostExecute(Response<ItemAdapter> response) {
			// finished with the background work
			setSupportProgressBarIndeterminateVisibility(false);

			if (response.getResponseCode() != ResponseCode.OK) {
				displayError(response.getResponseCode().getErrorString(), reloadTask);
			}

			else {
				hideError();
				// set list of borrowed items
				Log.w(TAG, "adapter count: " + response.getData().getCount());
				Log.w(TAG, String.format("error=%s, list=%s",
						findViewById(R.id.error_container).getVisibility(),
						borrowedItemListView.getVisibility()));
				borrowedItemListView.setAdapter(response.getData());
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		if (mActionMode != null) {
			adapter.setSelected(position);
			if (!adapter.itemSelected()) {
				mActionMode.finish();
			}
		} else {
			Item item = (Item) parent.getItemAtPosition(position);
			Intent intent = new Intent(this, ItemDetailActivity.class);
			intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
			startActivity(intent);
		}
	}

	public void showReturnDialog(final List<Item> items) {
		Log.i(TAG, "items zurückgeben count=" + items.size());

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_title_return_items);
		dialog.setMessage(getResources().getQuantityString(R.plurals.dialog_message_return_items, items.size(), items.size()));
		dialog.setPositiveButton(R.string.return_item, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Item[] itemTypeArray = new Item[0];

				new ReturnItemTask().execute(items.toArray(itemTypeArray));
				mActionMode.finish();
			}
		});
		dialog.setNegativeButton(R.string.cancel, null);
		dialog.show();
	}

	/**
	 * calls the context menu
	 */
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// context menu inflater
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.welcome_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.item_return:
				showReturnDialog(adapter.getSelected());
				return true;

			case R.id.item_bibtex:
				String bibTex = BibtexUtil.getBibtex(adapter.getSelected());
				Log.i(TAG, "bibtex for " + adapter.getSelected().size() + " items");
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "BibTeX");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, bibTex);
				startActivity(Intent.createChooser(sharingIntent, getString(R.string.bibtex_share_title)));
				return true;

			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			// delete selectedItems when closed
			if (adapter.itemSelected()) {
				adapter.wipeSelectedItems();
				adapter.notifyDataSetChanged();
			}

		}
	};

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.i("WelcomeActivity", "invoked onItemLongClick");
		if (mActionMode != null) {
			return false;
		}

		// Start the CAB using the ActionMode.Callback defined above mActionMode
		mActionMode = this.startActionMode(mActionModeCallback);

		// make list multiple selectable
		borrowedItemListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		// sets the clicked item as first selected item

		adapter.setSelected(position);

		return true;
	}

	private class ReturnItemTask extends AsyncTask<Item, Void, Void> {

		@Override
		protected Void doInBackground(Item... params) {
			for (Item itemToReturn : params) {
				Log.i(TAG, "Returning " + itemToReturn.toString());

				PocketBibApp.getLibraryManager().returnItem(itemToReturn);
			}

			reloadTask.execute();
			return null;
		}

	}

}