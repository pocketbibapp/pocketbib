package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity;
import edu.kit.tm.telematics.pocketbib.controller.activity.ItemDetailActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.BibtexUtil;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;

public class ManageBorrowedItemsActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

	private LoggedInUser user;
	private ListView borrowedItemListView;
	private TextView noItemsTextView;
	private ItemAdapter adapter;
	private ActionMode mActionMode = null;
	private final static String TAG = "ManageBorrowedItemsActivity";

	private ReloadTask<ItemAdapter> reloadTask = new ReloadTask<ItemAdapter>() {
		@Override
		public Response<ItemAdapter> doInBackground() {
			return PocketBibApp.getLibraryManager().getBorrowedItems(user);
		}

		@Override
		public void onPostExecute(Response<ItemAdapter> response) {
			if (response.getResponseCode() == ResponseCode.OK) {
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
		user = (LoggedInUser) getIntent().getParcelableExtra(Constants.Intent.KEY_USER_PARCEL);
		Assert.assertNotNull(user);
		Log.i(TAG, "Username: " + user.getFirstName() + user.getLastName());
		// adds a indeterminate progress bar and sets the visibility to
		// false
		// this is used to show that this activity is trying to get
		// information from the internet
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.activity_admin_manage_borrowed_items);
		borrowedItemListView = (ListView) findViewById(R.id.list_borrowed_items);
		noItemsTextView = (TextView) findViewById(R.id.label_no_items);
		borrowedItemListView.setEmptyView(noItemsTextView);
		noItemsTextView.setText(R.string.label_no_items);
		borrowedItemListView.setOnItemClickListener(this);
		borrowedItemListView.setOnItemLongClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// hide contextualActionBarSherlock
		if (mActionMode != null)
			mActionMode.finish();
		if (user != null)
			new SetViewTask().execute();

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
			intent.putExtra("item", item);
			startActivity(intent);
		}
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
				ArrayList<Item> items = adapter.getSelected();
				Log.i(TAG, "items zurückgeben count=" + items.size());

				Item[] itemTypeArray = new Item[0];

				new ReturnItemTask().execute(items.toArray(itemTypeArray));
				mActionMode.finish();
				return true;

			case R.id.item_bibtex:
				String bibTex = BibtexUtil.getBibtex(adapter.getSelected());
				Log.i(TAG, "bibtex: für " + adapter.getSelected().size() + "Items");
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "BibTex");
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
		Log.i(TAG, "invoked onItemLongClick");
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
			if (user != null) {
				Response<ItemAdapter> response = PocketBibApp.getLibraryManager().getBorrowedItems(user);
				adapter = response.getData();
				Log.i(TAG, "Borrowed Items: " + adapter.toString());
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
				// set list of borrowed items
				borrowedItemListView.setAdapter(adapter);

			}
		}
	}
}
