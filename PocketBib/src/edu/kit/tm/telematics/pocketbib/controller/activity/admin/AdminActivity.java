package edu.kit.tm.telematics.pocketbib.controller.activity.admin;

import junit.framework.Assert;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.activity.BaseActivity;
import edu.kit.tm.telematics.pocketbib.controller.activity.WelcomeActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortField;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortOrder;
import edu.kit.tm.telematics.pocketbib.model.user.User;

public class AdminActivity extends BaseActivity {

	/** The fragment that is currently selected */
	private AdminFragment activeFragment = null;

	private Menu menu;

	/** Tag for debbuging purposes */
	private final static String TAG = "AdminActivity";

	/** The two fragments that are shown */
	private AdminFragment itemsFragment, usersFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin);

		setDisplayShowUp(WelcomeActivity.class, true);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab itemsTab = actionBar.newTab().setText(R.string.label_tab_items);
		ActionBar.Tab usersTab = actionBar.newTab().setText(R.string.label_tab_users);

		itemsFragment = new ItemSearchFragment();
		usersFragment = new UserSearchFragment();

		itemsTab.setTabListener(new TabListener(itemsFragment));
		usersTab.setTabListener(new TabListener(usersFragment));

		actionBar.addTab(itemsTab);
		actionBar.addTab(usersTab);
		
		
		Integer index = 0;
		if(getIntent().hasExtra(Constants.Intent.KEY_ADMIN_ACTIVE_TAB)) {
			index = getIntent().getIntExtra(Constants.Intent.KEY_ADMIN_ACTIVE_TAB, -1);
			
			if(index != 0 && index != 1)
				throw new AssertionError();
		} else {
			Integer last = (Integer) getLastCustomNonConfigurationInstance();
			
			if(last != null) {
				index = last.intValue();
			}
		}
		
		if (index != null && index.intValue() == 1) {
			setActiveFragment(usersFragment);
			getSupportActionBar().setSelectedNavigationItem(1);
		} else {
			if(index != null && index.intValue() != 1 && index.intValue() != 2) {
				Log.e(TAG, "Undefined configuration instance " + index.intValue() + "(" + index + ")");
			}
			
			setActiveFragment(itemsFragment);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!User.getCurrentUser().isAdministrator()) {
			Log.e(TAG, "Non-admin user tried to access AdminActivity");
			startActivity(new Intent(this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.admin, menu);

		menu.findItem(R.id.item_my_account).setVisible(false);
		menu.findItem(R.id.item_administration).setVisible(false);

		this.menu = menu;

		activeFragment.editSortMenu(menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem selectedItem) {
		int checkedItem;
		
		switch (selectedItem.getItemId()) {
		case R.id.item_add:
			activeFragment.onAddClick();
			return true;

		case R.id.item_sort_by_date_asc: 
			activeFragment.sort(SortField.DATE, SortOrder.ASC);
			checkedItem = R.id.item_sort_by_date;
			break;
			
		case R.id.item_sort_by_date_desc:
			activeFragment.sort(SortField.DATE, SortOrder.DESC);
			checkedItem = R.id.item_sort_by_date;
			break;

		case R.id.item_sort_by_name_asc:
			activeFragment.sort(SortField.NAME, SortOrder.ASC);
			checkedItem = R.id.item_sort_by_name;
			break;

		case R.id.item_sort_by_name_desc:
			activeFragment.sort(SortField.NAME, SortOrder.DESC);
			checkedItem = R.id.item_sort_by_name;
			break;
		
		case R.id.item_sort_by_rating_asc:
			activeFragment.sort(SortField.RATING, SortOrder.ASC);
			checkedItem = R.id.item_sort_by_rating;
			break;
			
		case R.id.item_sort_by_rating_desc:
			activeFragment.sort(SortField.RATING, SortOrder.DESC);
			checkedItem = R.id.item_sort_by_rating;
			break;
			
		case R.id.item_sort_by_title_asc:
			activeFragment.sort(SortField.TITLE, SortOrder.ASC);
			checkedItem = R.id.item_sort_by_title;
			break;
			
		case R.id.item_sort_by_title_desc:
			activeFragment.sort(SortField.TITLE, SortOrder.DESC);
			checkedItem = R.id.item_sort_by_title;
			break;

		case R.id.item_sort_by_relevance:
			activeFragment.sort(SortField.RELEVANCE, SortOrder.DESC);
			checkedItem = R.id.item_sort_by_relevance;
			break;

		case R.id.item_admin_search:
			activeFragment.onSearchClick();
			return true;

		default:
			return super.onOptionsItemSelected(selectedItem);
		}
		
		Assert.assertTrue(selectedItem.isCheckable());
		
		// uncheck all main sort orders (name, rating, date, ...)
		Menu searchMenu = menu.findItem(R.id.item_sort).getSubMenu();
		for (int i = 0; i < searchMenu.size(); i++) {
			MenuItem item = searchMenu.getItem(i);
			item.setChecked(false);
			
			if(item.hasSubMenu()) {
				// uncheck all items
				Menu subMenu = item.getSubMenu();
				for (int j = 0; j < subMenu.size(); j++) {
					subMenu.getItem(j).setChecked(false);
				}
			}
		}
		
		// check the currently selected sort order again
		MenuItem parentItem = menu.findItem(checkedItem);
		parentItem.setChecked(true);
		
		// check the current sort order again
		selectedItem.setChecked(true);
		
		return true;
	}

	private void setActiveFragment(AdminFragment f) {
		activeFragment = f;
		f.editSortMenu(menu);

	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		if (activeFragment instanceof ItemSearchFragment)
			return Integer.valueOf(0);
		else if (activeFragment instanceof UserSearchFragment)
			return Integer.valueOf(1);
		return null;
	}

	private class TabListener implements com.actionbarsherlock.app.ActionBar.TabListener {

		private final AdminFragment fragment;

		public TabListener(AdminFragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, fragment);
			setActiveFragment(fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	}
}
