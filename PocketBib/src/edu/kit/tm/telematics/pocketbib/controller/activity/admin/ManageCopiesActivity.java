package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopy;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopyAdapter;
import edu.kit.tm.telematics.pocketbib.view.DialogUtil;

public class ManageCopiesActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener {

	private Item item;
	private ListView copyList;
	private ListView itemList;
	private ActionMode mActionMode;
	private ItemCopyAdapter adapter;
	private final static String TAG = "ManageCopiesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		item = (Item) getIntent().getParcelableExtra(Constants.Intent.KEY_ITEM_PARCEL);
		setDisplayShowUp(ItemAddEditActivity.class, true);

		setSupportProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.activity_admin_manage_copies);

		itemList = (ListView) findViewById(R.id.list_item);

		List<Item> itemCollection = new ArrayList<Item>(1);
		itemCollection.add(item);
		itemList.setAdapter(new ItemAdapter(this, itemCollection));

		copyList = (ListView) findViewById(R.id.list_item_copies);
		copyList.setOnItemClickListener(this);
		copyList.setOnItemLongClickListener(this);
	}

	@Override
	protected void onResume() {
		new SetViewTask().execute();

		// hide contextualActionBarSherlock
		if (mActionMode != null)
			mActionMode.finish();

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.manage_copies, menu);

		menu.findItem(R.id.item_administration).setVisible(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemID = item.getItemId();
		switch (itemID) {
		case R.id.item_add_copy:
			if (Constants.API_LEVEL >= 11)
				displayAddCopiesDialog();
			else
				displayFallbackAddCopiesDialog();

			return true;

		case android.R.id.home:
			startActivity(new Intent(this, ItemAddEditActivity.class).putExtra(Constants.Intent.KEY_ITEM_PARCEL,
					this.item).putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_EDIT_MODE));
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void displayFallbackAddCopiesDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_action_add_copies_title);
		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		final Spinner spinner = (Spinner) li.inflate(R.layout.show_dialog_number_picker, null);

		dialog.setView(spinner);
		dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				addCopies(spinner.getSelectedItemPosition() + 1);

			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.show();
	}

	@TargetApi(11)
	private void displayAddCopiesDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_action_add_copies_title);

		final NumberPicker numberPicker = new NumberPicker(this);
		numberPicker.setMinValue(1);
		numberPicker.setMaxValue(50);
		numberPicker.setWrapSelectorWheel(false);
		numberPicker.setValue(1);
		dialog.setView(numberPicker);

		dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				addCopies(numberPicker.getValue());

			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.show();
	}

	private void addCopies(final int num) {
		DialogUtil.showProgressDialog(this, R.string.saving, null, new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < num; i++) {
					ItemCopy copy = ItemCopy.createNew(item);
					Response<?> response = PocketBibApp.getLibraryManager().insertOrUpdateCopy(copy);
					Log.i(TAG, "Added copy - Response: " + response.getResponseCode());
				}

				reloadTask.execute();
			}
		});
	}

	private ReloadTask<ItemCopyAdapter> reloadTask = new ReloadTask<ItemCopyAdapter>() {

		@Override
		public Response<ItemCopyAdapter> doInBackground() {
			return PocketBibApp.getLibraryManager().getItemCopies(item.getItemId());
		}

		@Override
		public void onPostExecute(Response<ItemCopyAdapter> response) {

			if (response.getResponseCode() == ResponseCode.OK) {
				adapter = response.getData();
				copyList.setAdapter(adapter);
			} else {
				displayError(response.getResponseCode().getErrorString(), this);
			}
		}

	};

	protected class SetViewTask extends AsyncTask<Void, Void, Response<ItemCopyAdapter>> {

		@Override
		protected void onPreExecute() {
			// shows that the activity is working in the background
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Response<ItemCopyAdapter> doInBackground(Void... arg0) {
			Log.i(TAG, item.toString());
			Response<ItemCopyAdapter> response = PocketBibApp.getLibraryManager().getItemCopies(item.getItemId());
			Log.i(TAG, response.getData().toString());
			adapter = response.getData();
			return response;

		}

		@Override
		protected void onCancelled() {
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPostExecute(Response<ItemCopyAdapter> response) {
			if (response.getResponseCode() == ResponseCode.OK)
				copyList.setAdapter(response.getData());
			else
				displayError(response.getResponseCode().getErrorString(), reloadTask);

			// finished with the background work
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	protected class DeleteCopiesTask extends AsyncTask<ItemCopy, Void, ResponseCode> {

		@Override
		protected ResponseCode doInBackground(ItemCopy... params) {
			ResponseCode response = ResponseCode.OK;
			ResponseCode temp;
			
			if(params != null) {				
				for (ItemCopy copy : params) {
					temp = PocketBibApp.getLibraryManager().removeCopy(copy);
					if (temp != ResponseCode.OK)
						response = temp;
				}
			}
			
			reloadTask.execute();
			
			return response;
		}

		@Override
		protected void onPostExecute(ResponseCode result) {
			if (result != ResponseCode.OK) {
				DialogUtil.showErrorDialog(ManageCopiesActivity.this, result.getErrorStringRes());
			}
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
			inflater.inflate(R.menu.manage_copies_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.item_delete:
				final ArrayList<ItemCopy> items = adapter.getSelected();
				Log.i(TAG, "delete copies count=" + items.size());

				AlertDialog.Builder builder = new AlertDialog.Builder(ManageCopiesActivity.this);
				builder.setTitle(R.string.dialog_title_delete);
				builder.setMessage(getResources().getQuantityString(R.plurals.dialog_message_delete_multiple,
						items.size(), items.size()));

				builder.setPositiveButton(R.string.menu_delete, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new DeleteCopiesTask().execute(items.toArray(new ItemCopy[] {}));
					}
				});
				builder.setNegativeButton(R.string.cancel, null);
				builder.show();

				mode.finish();
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

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mActionMode != null) {
			return false;
		}

		// Start the CAB using the ActionMode.Callback defined above
		mActionMode = this.startActionMode(mActionModeCallback);

		// make list multiple selectable
		copyList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		// sets the clicked item as first selected item
		adapter.setSelected(position);

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (mActionMode != null) {
			adapter.setSelected(position);
			if (!adapter.itemSelected()) {
				mActionMode.finish();
			}
		}
	}
}