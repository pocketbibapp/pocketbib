package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import java.util.ArrayList;
import java.util.Comparator;

import junit.framework.Assert;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity.ReloadRunnable;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity.ReloadTask;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortField;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortOrder;
import edu.kit.tm.telematics.pocketbib.model.ItemAlphabetComparator;
import edu.kit.tm.telematics.pocketbib.model.ItemDateComparator;
import edu.kit.tm.telematics.pocketbib.model.ItemRatingComparator;
import edu.kit.tm.telematics.pocketbib.model.ItemRelevanceComparator;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;

/**
 * Fragment for the AdminActivity that displays a list of user accounts.
 */
public class ItemSearchFragment extends AdminFragment {

	private ItemAdapter adapter;

	private final static String TAG = "ItemSearchFragment";

	private Comparator<Item> comparator = ItemRelevanceComparator.DESC;

	@Override
	@TargetApi(11)
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

		((TextView) layout.findViewById(R.id.empty)).setText(R.string.label_no_items_found);

		return layout;
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		// the search view has position 0
		Item item = adapter.getItem(position - 1);

		if (item != null) {
			Intent intent = new Intent(getBaseActivity(), ItemAddEditActivity.class);
			intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_EDIT_MODE);
			intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
			startActivity(intent);
		} else {
			Log.e(TAG, "Clicked on invalid list item [position=" + position + " item=" + item);
		}

	}

	@Override
	public void sort(SortField sortField, SortOrder sortOrder) {
		Assert.assertNotNull(sortField);
		Assert.assertNotNull(sortOrder);

		switch (sortField) {
		case TITLE:
			if (sortOrder == SortOrder.DESC)
				comparator = ItemAlphabetComparator.DESC;
			else
				comparator = ItemAlphabetComparator.ASC;
			break;

		case RELEVANCE:
			if (sortOrder == SortOrder.DESC)
				comparator = ItemRelevanceComparator.DESC;
			else
				comparator = ItemRelevanceComparator.ASC;
			break;

		case DATE:
			if (sortOrder == SortOrder.DESC)
				comparator = ItemDateComparator.DESC;
			else
				comparator = ItemDateComparator.ASC;
			break;

		case RATING:
			if (sortOrder == SortOrder.DESC)
				comparator = ItemRatingComparator.DESC;
			else
				comparator = ItemRatingComparator.ASC;
			break;
		default:
			Log.e(TAG, "Invalid sortOrder for items: " + sortField);
		}

		adapter.sort(comparator);
	}

	@Override
	public void onAddClick() {
		Intent intent = new Intent(getBaseActivity(), ItemAddEditActivity.class);
		intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_ADD_MODE);
		startActivity(intent);
	}

	@Override
	public void editSortMenu(Menu menu) {
		if (menu != null) {
			menu.findItem(R.id.item_sort_by_date).setVisible(true);
			menu.findItem(R.id.item_sort_by_name).setVisible(false);
			menu.findItem(R.id.item_sort_by_rating).setVisible(true);
			menu.findItem(R.id.item_sort_by_relevance).setVisible(true);
			menu.findItem(R.id.item_sort_by_title).setVisible(true);
		}
	}

	public ReloadRunnable getReloadRunnable() {
		return reloadRunnable;
	}

	/** Runnable that reloads the item list */
	private final ReloadRunnable reloadRunnable = new ReloadTask<ItemAdapter>() {
		@Override
		public Response<ItemAdapter> doInBackground() {
			final Response<ItemAdapter> response;

			if (query == null || query.equals("")) {
				response = new Response<ItemAdapter>(ResponseCode.OK, new ItemAdapter(getBaseActivity(),
						new ArrayList<Item>()));
			} else {
				response = PocketBibApp.getLibraryManager().searchLibrary(query, true);
			}

			// Log.e("query", query);
			adapter = response.getData();

			return response;
		}

		@Override
		public void onPostExecute(Response<ItemAdapter> response) {
			if (response.getResponseCode() == ResponseCode.OK) {
				Assert.assertNotNull("With a ResponseCode.OK response, the adapter may not be null.", adapter);

				if (getBaseActivity() != null) {
					getBaseActivity().hideError();
				}

				adapter.sort(comparator);
				setListAdapter(adapter);

				if (query == null || query.trim().equals(""))
					emptyTextView.setText(R.string.label_enter_query);
				else
					emptyTextView.setText(R.string.label_no_items_found);

			} else {
				Log.i(TAG, "setListAdapter(null) caused by ResponseCode " + response.getResponseCode());
				setListAdapter(null);
				progressBar.setVisibility(View.GONE);
				getBaseActivity().displayError(response.getResponseCode().getErrorString(), this);
			}
		}
	};
}
