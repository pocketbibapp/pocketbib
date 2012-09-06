package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity.ReloadRunnable;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortField;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortOrder;

public abstract class AdminFragment extends ListFragment {

	protected String query;
	
	protected ViewGroup searchLayout;

	protected ProgressBar progressBar;

	protected TextView emptyTextView;

	protected ViewGroup layout;
	
	private int lastFirstVisibleItem = 0;
	
	@Override
	@TargetApi(11)
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (ViewGroup) inflater.inflate(R.layout.fragment_admin_user_list, null);
		progressBar = (ProgressBar) layout.findViewById(R.id.progress);
		emptyTextView = (TextView) layout.findViewById(R.id.empty);

		getBaseActivity().initializeErrorLayout(layout.findViewById(R.id.error_container));

		searchLayout = (ViewGroup) inflater.inflate(R.layout.include_search_view, null);
		ListView list = (ListView) layout.findViewById(android.R.id.list);
		list.addHeaderView(searchLayout);
		list.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem >= 1 && lastFirstVisibleItem == 0) {
					InputMethodManager imm = (InputMethodManager) getBaseActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(layout.getApplicationWindowToken(), 0);
				}

				lastFirstVisibleItem = firstVisibleItem;
			}
		});

		final EditText searchView = (EditText) layout.findViewById(R.id.search);
		searchView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				requestSearch(searchView.getText().toString());
				return false;
			}
		});

		layout.findViewById(R.id.button_cancel_search).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchView.setText("");
				requestSearch("");
			}
		});

		layout.findViewById(R.id.button_search).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				requestSearch(searchView.getText().toString());
			}
		});

		return layout;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		setListAdapter(null);
		new Thread(getReloadRunnable()).start();
	}
	
	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}
	
	@TargetApi(8)
	public void onSearchClick() {
		searchLayout.requestFocus();

		InputMethodManager imm = (InputMethodManager) getBaseActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		if (Constants.API_LEVEL >= 8)
			getListView().smoothScrollToPosition(0);
		else
			getListView().setSelection(0);
	}
	
	@Override
	public void setListAdapter(ListAdapter adapter) {
		if(adapter == null) {
			// display a progress bar to indicate loading
			
			progressBar.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.GONE);
		} else if(adapter.getCount() == 0) {
			// the adapter is empty -> display 'no items found'
			
			progressBar.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.VISIBLE);
		} else {
			// the adapter contains items, display the list
			
			progressBar.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
		}
		
		super.setListAdapter(adapter);
	}
	
	public void requestSearch(String query) {
		setListAdapter(null);
		this.query = query.trim();

		getBaseActivity().hideError();
		
		InputMethodManager imm = (InputMethodManager) getBaseActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(layout.getApplicationWindowToken(), 0);
        
		onSearchRequested();
	}

	public void onSearchRequested() {
		new Thread(getReloadRunnable()).start();
	}
	
	public abstract void onListItemClick(ListView parent, View v, int position, long id);
	
	public abstract ReloadRunnable getReloadRunnable();
	
	public abstract void sort(SortField sortField, SortOrder sortOrder); 
	
	public abstract void onAddClick();
	
	public abstract void editSortMenu(Menu menu);
}
