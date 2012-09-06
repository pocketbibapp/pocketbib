package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

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
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.UserAlphabetComparator;
import edu.kit.tm.telematics.pocketbib.model.UserRelevanceComparator;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.UserAdapter;

/**
 * Fragment for the AdminActivity that displays a list of user accounts.
 */
public class UserSearchFragment extends AdminFragment {

	private UserAdapter adapter;

	private final static String TAG = "UserSearchFragment";

	private Comparator<LoggedInUser> comparator = UserAlphabetComparator.ASC;

	@Override
	@TargetApi(11)
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

		((TextView) layout.findViewById(R.id.empty)).setText(R.string.label_no_users_found);

		return layout;
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		// the search view has position 0
		LoggedInUser user = adapter.getItem(position - 1);

		if (user != null) {
			Intent intent = new Intent(getBaseActivity(), UserAddEditActivity.class);
			intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_EDIT_MODE);
			intent.putExtra(Constants.Intent.KEY_USER_PARCEL, user);
			startActivity(intent);
		} else {
			Log.e(TAG, "Clicked on invalid list item [position=" + position + " user=" + user);
		}
	}

	@Override
	public void sort(SortField sortField, SortOrder sortOrder) {
		Assert.assertNotNull(sortField);
		Assert.assertNotNull(sortOrder);

		switch (sortField) {
		case NAME:
			if (sortOrder == SortOrder.DESC)
				comparator = UserAlphabetComparator.DESC;
			else
				comparator = UserAlphabetComparator.ASC;
			break;

		case RELEVANCE:
			if (sortOrder == SortOrder.DESC)
				comparator = UserRelevanceComparator.DESC;
			else
				 comparator = UserRelevanceComparator.ASC;
			break;

		default:
			Log.e(TAG, "Invalid sortOrder for users: " + sortField);
		}

		adapter.sort(comparator);
	}

	@Override
	public void onAddClick() {
		Intent intent = new Intent(getBaseActivity(), UserAddEditActivity.class);
		intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_ADD_MODE);
		startActivity(intent);
	}

	@Override
	public void editSortMenu(Menu menu) {
		if (menu != null) {
			menu.findItem(R.id.item_sort_by_date).setVisible(false);
			menu.findItem(R.id.item_sort_by_name).setVisible(true);
			menu.findItem(R.id.item_sort_by_rating).setVisible(false);
			menu.findItem(R.id.item_sort_by_relevance).setVisible(true);
			menu.findItem(R.id.item_sort_by_title).setVisible(false);
		}
	}

	public ReloadRunnable getReloadRunnable() {
		return reloadRunnable;
	}

	/** Runnable that reloads the user list */
	private final ReloadRunnable reloadRunnable = new ReloadTask<UserAdapter>() {
		@Override
		public Response<UserAdapter> doInBackground() {
			final Response<UserAdapter> response;

			if (query == null || query.length() == 0)
				response = PocketBibApp.getRegistrationProvider().getUsers();
			else
				response = PocketBibApp.getRegistrationProvider().searchUsers(query);

			adapter = response.getData();

			if (adapter != null)
				Log.i(TAG, "Query: '" + query + "' -> " + adapter.getCount() + " results.");
			else
				Log.i(TAG, "Query: '" + query + "' -> NULL-adapter");

			return response;
		}

		@Override
		public void onPostExecute(Response<UserAdapter> response) {
			if (response.getResponseCode() == ResponseCode.OK) {
				Assert.assertNotNull("With a ResponseCode.OK response, the adapter may not be null.", adapter);

				if (getBaseActivity() != null) {
					getBaseActivity().hideError();
				}
				
				if (comparator != UserRelevanceComparator.DESC) {
					adapter.sort(comparator);
				}
				
				setListAdapter(adapter);
			} else {
				Log.i(TAG, "setListAdapter(null) caused by ResponseCode " + response.getResponseCode());
				setListAdapter(null);
				progressBar.setVisibility(View.GONE);
				getBaseActivity().displayError(response.getResponseCode().getErrorString(), this);
			}
		}
	};
}
