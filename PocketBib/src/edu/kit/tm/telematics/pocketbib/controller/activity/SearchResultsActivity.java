package edu.kit.tm.telematics.pocketbib.controller.activity;

import static edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters.KEY_AUTHOR;
import static edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters.KEY_ISBN;
import static edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters.KEY_ISSN;
import static edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters.KEY_PUBLICATION_YEAR;
import static edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters.KEY_PUBLISHER;
import static edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters.KEY_TITLE;
import static junit.framework.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortField;
import edu.kit.tm.telematics.pocketbib.model.Constants.SortOrder;
import edu.kit.tm.telematics.pocketbib.model.ItemAlphabetComparator;
import edu.kit.tm.telematics.pocketbib.model.ItemDateComparator;
import edu.kit.tm.telematics.pocketbib.model.ItemRatingComparator;
import edu.kit.tm.telematics.pocketbib.model.ItemRelevanceComparator;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Isbn;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;

/**
 * This activites is responsible for all search requests.
 */
public class SearchResultsActivity extends BaseActivity {

	/** Tag for logging purposes. */
	private static final String TAG = "SearchResultsActivity";

	/**
	 * The request code for voice input to be used for the simple search.
	 * 
	 * @see #onActivityResult(int, int, Intent)
	 */
	private static final int VOICE_REQUEST = 0;

	/**
	 * Creates a {@link HashMap} of a extended search query from the JSON
	 * string. This is used to convert the information passed in the intent by
	 * the {@link ExtendedSearchFormActivity}.
	 * 
	 * @param jsonString
	 *            the JSON with the search query
	 * @return the search as a hash map
	 */
	private static HashMap<String, String> createQuery(String jsonString) {
		if (jsonString == null) {
			Log.w(TAG, "createQuery was called without a jsonString.");
			return null;
		}

		JSONObject json = null;

		try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		HashMap<String, String> result = new HashMap<String, String>();

		if (json.has(KEY_TITLE))
			result.put(KEY_TITLE, json.optString(KEY_TITLE));
		if (json.has(KEY_AUTHOR))
			result.put(KEY_AUTHOR, json.optString(KEY_AUTHOR));
		if (json.has(KEY_PUBLISHER))
			result.put(KEY_PUBLISHER, json.optString(KEY_PUBLISHER));
		if (json.has(KEY_PUBLICATION_YEAR))
			result.put(KEY_PUBLICATION_YEAR, json.optString(KEY_PUBLICATION_YEAR));
		if (json.has(KEY_ISBN))
			result.put(KEY_ISBN, json.optString(KEY_ISBN));
		if (json.has(KEY_ISSN))
			result.put(KEY_ISSN, json.optString(KEY_ISSN));

		return result;
	}

	/**
	 * Creates a String of an {@link JSONObject} of a extended search query from map object.
	 * This is used to convert the information so it can be passed by an intent.
	 * 
	 * @param map the map with the search query
	 * @return the search as string
	 */
	private static String createJsonString(Map<String, String> map) {
		if (map == null) {
			Log.w(TAG, "createJsonString was called without a map.");
			return null;
		}

		JSONObject json = new JSONObject();
		try {
			if (map.containsKey(KEY_TITLE))
				json.put(KEY_TITLE, map.get(KEY_TITLE));
			if (map.containsKey(KEY_AUTHOR))
				json.put(KEY_AUTHOR, map.get(KEY_AUTHOR));
			if (map.containsKey(KEY_PUBLISHER))
				json.put(KEY_PUBLISHER, map.get(KEY_PUBLISHER));
			if (map.containsKey(KEY_PUBLICATION_YEAR))
				json.put(KEY_PUBLICATION_YEAR, map.get(KEY_PUBLICATION_YEAR));
			if (map.containsKey(KEY_ISBN))
				json.put(KEY_ISBN, map.get(KEY_ISBN));
			if (map.containsKey(KEY_ISSN))
				json.put(KEY_ISSN, map.get(KEY_ISSN));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return json.toString();

	}

	/** The ListView with the search results. */
	private ListView searchResultsListView;

	/** The type of search performed. */
	private String queryType = null;

	/** A simple query */
	private String simpleQuery = null;

	/** A extended query */
	private Map<String, String> extendedQuery = null;

	/** The search box for the simple search query */
	private EditText searchView;
	
	/** The message that is shown instead an empty results list */
	private TextView searchMessage;

	/** The item that is currently shown at the very top of the screen */
	private int lastFirstVisibleItem = 0;

	/** The sort order for the search result */
	private Comparator<Item> comparator = ItemRelevanceComparator.DESC;

	private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDisplayShowUp(WelcomeActivity.class, true);
		setContentView(R.layout.activity_searchresults);
		
		searchMessage = (TextView) findViewById(R.id.search_no_items);

		searchResultsListView = (ListView) findViewById(R.id.search_results);
		View searchBar = getLayoutInflater().inflate(R.layout.include_search_view, null);
		searchResultsListView.addHeaderView(searchBar);
		searchResultsListView.setOnItemClickListener(onListItemClick);
		searchResultsListView.setOnScrollListener(onSearchResultsScroll);
		if (searchResultsListView.getAdapter() == null)
			searchResultsListView.setAdapter(new ItemAdapter(this, new ArrayList<Item>()));

		searchView = (EditText) findViewById(R.id.search);
		searchView.setVisibility(View.VISIBLE);
		if (savedInstanceState != null)
			searchView.setText(savedInstanceState.getString("searchView"));
		searchView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				simpleQuery = searchView.getText().toString();				
				new Thread(reloadRunnable).start();
				return false;
			}
		});

		findViewById(R.id.button_cancel_search).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				simpleQuery = "";
				searchView.setText(simpleQuery);
				new Thread(reloadRunnable).start();
			}
		});

		findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchResultsListView.getWindowToken(), 0);
				
				simpleQuery = searchView.getText().toString();				
				new Thread(reloadRunnable).start();
			}
		});

		// distinguishes the different kinds of searches performed
		Intent intent = getIntent();
		queryType = savedInstanceState != null ? savedInstanceState.getString(Constants.KEY_QUERY_TYPE) :
			intent.getStringExtra(Constants.KEY_QUERY_TYPE);
		
		if (Constants.VALUE_QUERY_TYPE_SIMPLE.equals(queryType)) {
			if (savedInstanceState != null) {
				simpleQuery = savedInstanceState.getString(Constants.KEY_QUERY_DATA);
				searchView.setText(simpleQuery);
				Log.i(TAG, "Starting activity in simple mode with query: " + simpleQuery);				
				new Thread(reloadRunnable).start();
			} else {
				onSearchClick();
			}

		} else if (Constants.VALUE_QUERY_TYPE_EXTENDED.equals(queryType)) {
			getSupportActionBar().setTitle(R.string.title_extended_search);
			searchResultsListView.removeHeaderView(searchBar);
			
			String query = savedInstanceState != null ? savedInstanceState.getString(Constants.KEY_QUERY_DATA) : 
				intent.getStringExtra(Constants.KEY_QUERY_DATA);
			Log.i(TAG, "Starting activity with extended mode with query: " + query);
			extendedQuery = createQuery(query);
			new Thread(reloadRunnable).start();

		} else if (Constants.VALUE_QUERY_TYPE_SCAN_BARCODE.equals(queryType)) {
			doBarcodeSearch();

		} else if (Constants.VALUE_QUERY_TYPE_VOICE.equals(queryType)) {
			doVoiceSearch();

		} else {
			Log.e(TAG, "SearchResultsActivity was started without (correct) intent information for query");
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		queryType = Constants.VALUE_QUERY_TYPE_SIMPLE;
		super.onActivityResult(requestCode, resultCode, data);
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			String query = scanResult.getContents();

			Log.i(TAG, "Barcode Results: " + query);
			if (query != null && query.length() > 0) {
				simpleQuery = query;
				searchView.setText(simpleQuery);
				new Thread(reloadRunnable).start();
			} else if (query == null && simpleQuery == null && extendedQuery == null){
				finish();
			}

		} else if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case VOICE_REQUEST:
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				Log.i(TAG, "Voice Results: " + matches);
				simpleQuery = matches.get(0);
				searchView.setText(simpleQuery);
				if (simpleQuery != null && simpleQuery.length() > 2) {					
					new Thread(reloadRunnable).start();
				} else {
					searchMessage.setText(R.string.query_too_short);
					searchMessage.setVisibility(View.VISIBLE);
				}
				break;

			default:
				throw new AssertionError("Unknown RequestCode " + simpleQuery);
			}
		} else {
			Log.e(TAG, "SearchResultsActivity got unexpected results from another activity.");
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		setLoginLogoutMenuItemVisible(menu, true);
		menu.findItem(R.id.item_search).setVisible(true);

		getSupportMenuInflater().inflate(R.menu.search_results, menu);
		
		this.menu = menu;
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (simpleQuery != null)
			outState.putString(Constants.KEY_QUERY_DATA, simpleQuery);
		if (extendedQuery != null)
			outState.putString(Constants.KEY_QUERY_DATA, createJsonString(extendedQuery));
		outState.putString("searchView", searchView.getText().toString());
		outState.putString(Constants.KEY_QUERY_TYPE, queryType);
	}

	private OnItemClickListener onListItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Item item = (Item) parent.getItemAtPosition(position);
			Intent intent = new Intent(SearchResultsActivity.this, ItemDetailActivity.class);
			intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
			intent.putExtra(Constants.Intent.KEY_CALLING_INTENT, Constants.Intent.VALUE_SEARCH_RESULTS_ACTIVITY);
			startActivity(intent);
		}
	};

	private OnScrollListener onSearchResultsScroll = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (firstVisibleItem >= 1 && lastFirstVisibleItem == 0) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchResultsListView.getWindowToken(), 0);
			}

			lastFirstVisibleItem = firstVisibleItem;
		}
	};

	@TargetApi(8)
	public void onSearchClick() {
		searchView.requestFocus();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

		if (Constants.API_LEVEL >= 8) {
			searchResultsListView.smoothScrollToPosition(0);
		} else {
			searchResultsListView.setSelection(0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home
				&& Constants.VALUE_QUERY_TYPE_EXTENDED.equals(queryType)) {
			finish();
			return true;
		}
		
		int checkedItem;
		switch (item.getItemId()) {
		case R.id.item_search_voice:
			doVoiceSearch();

			if (simpleQuery == null && extendedQuery == null)
				finish();
			return true;

		case R.id.item_search_scan_barcode:
			doBarcodeSearch();

			if (simpleQuery == null && extendedQuery == null)
				finish();
			return true;

		case R.id.item_search_text:
			if (searchResultsListView.getHeaderViewsCount() == 0) {
				getSupportActionBar().setTitle(R.string.title_search_results);
				searchResultsListView.setAdapter(null);
				searchResultsListView.addHeaderView(getLayoutInflater().inflate(R.layout.include_search_view, null));
				searchResultsListView.setAdapter(new ItemAdapter(SearchResultsActivity.this, new ArrayList<Item>()));
				searchMessage.setText(R.string.label_enter_query);
				searchMessage.setVisibility(View.VISIBLE);
			}
			
			queryType = Constants.VALUE_QUERY_TYPE_SIMPLE;
			onSearchClick();
			return true;


		case R.id.item_sort_by_date_asc: 
			comparator = ItemDateComparator.ASC;
			checkedItem = R.id.item_sort_by_date;
			break;

		case R.id.item_sort_by_date_desc:
			comparator = ItemDateComparator.DESC;
			checkedItem = R.id.item_sort_by_date;
			break;


		case R.id.item_sort_by_rating_asc:
			comparator = ItemRatingComparator.ASC;
			checkedItem = R.id.item_sort_by_rating;
			break;

		case R.id.item_sort_by_rating_desc:
			comparator = ItemRatingComparator.ASC;
			checkedItem = R.id.item_sort_by_rating;
			break;

		case R.id.item_sort_by_title_asc:
			comparator = ItemAlphabetComparator.ASC;
			checkedItem = R.id.item_sort_by_title;
			break;

		case R.id.item_sort_by_title_desc:
			comparator = ItemAlphabetComparator.DESC;
			checkedItem = R.id.item_sort_by_title;
			break;

		case R.id.item_sort_by_relevance:
			comparator = ItemRelevanceComparator.DESC;
			checkedItem = R.id.item_sort_by_relevance;
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		Assert.assertTrue(item.isCheckable());

		// uncheck all main sort orders (name, rating, date, ...)
		Menu searchMenu = menu.findItem(R.id.item_sort).getSubMenu();
		for (int i = 0; i < searchMenu.size(); i++) {
			MenuItem menuItem = searchMenu.getItem(i);
			menuItem.setChecked(false);

			if(menuItem.hasSubMenu()) {
				// uncheck all items
				Menu subMenu = menuItem.getSubMenu();
				for (int j = 0; j < subMenu.size(); j++) {
					subMenu.getItem(j).setChecked(false);
				}
			}
		}
		
		// check the currently selected sort order again
		MenuItem parentItem = menu.findItem(checkedItem);
		parentItem.setChecked(true);
		
		// check the current sort order again
		item.setChecked(true);
		new Thread(reloadRunnable).start();
		return true;
	}

	/**
	 * Sorts the search results;
	 * 
	 * @param sortField
	 *            selects the field that the search results are sorted by
	 * @param sortOrder
	 *            selects the order that the search results are sorted by
	 */
	// TODO at the moment the sort method crashes
	public void sort(SortField sortField, SortOrder sortOrder) {
		assertNotNull(sortField);
		assertNotNull(sortOrder);

		if (sortOrder == SortOrder.DESC) {
			switch (sortField) {
			case NAME:
				comparator = ItemAlphabetComparator.DESC;
				break;

			case RELEVANCE:
				comparator = ItemRelevanceComparator.DESC;
				break;

			case DATE:
				comparator = ItemDateComparator.DESC;
				break;

			case RATING:
				comparator = ItemRatingComparator.DESC;
				break;

			default:
				throw new AssertionError("Invalid sortField for items: " + sortField);
			}
		} else if (sortOrder == SortOrder.ASC) {
			switch (sortField) {
			case NAME:
				comparator = ItemAlphabetComparator.ASC;
				break;

			case RELEVANCE:
				comparator = ItemRelevanceComparator.ASC;
				break;

			case DATE:
				comparator = ItemDateComparator.ASC;
				break;

			case RATING:
				comparator = ItemRatingComparator.ASC;
				break;

			default:
				throw new AssertionError("Invalid sortField for items: " + sortField);
			}

		} else {
			throw new AssertionError("Invalid sortOrder for items: " + sortOrder);
		}
		new Thread(reloadRunnable).start();
	}
	
	/**
	 * Starts the voice recognition intent to be used for the simple search.
	 */
	private void doBarcodeSearch() {
		queryType = Constants.VALUE_QUERY_TYPE_SIMPLE;
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
	}

	/**
	 * Starts the voice recognition intent to be used for the simple search.
	 */
	private void doVoiceSearch() {
		queryType = Constants.VALUE_QUERY_TYPE_SIMPLE;
		Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(voiceIntent, VOICE_REQUEST);
	}

	/** Runnable that reloads the item list */
	private final ReloadRunnable reloadRunnable = new ReloadTask<ItemAdapter>() {
		public void onPreExecute() {
			searchMessage.setVisibility(View.GONE);
			findViewById(R.id.search_progress).setVisibility(View.VISIBLE);
		}

		@Override
		public Response<ItemAdapter> doInBackground() {
			final Response<ItemAdapter> response;
			if (simpleQuery != null && simpleQuery.length() > 2) {
				response = PocketBibApp.getLibraryManager().searchLibrary(simpleQuery, false);
			} else if (extendedQuery != null && extendedQuery.size() > 0) {
				response = PocketBibApp.getLibraryManager().searchLibrary(extendedQuery);
			} else {
				response = new Response<ItemAdapter>(ResponseCode.OK, new ItemAdapter(SearchResultsActivity.this,
						new ArrayList<Item>()));
			}
			return response;
		}

		@Override
		public void onPostExecute(Response<ItemAdapter> response) {
			findViewById(R.id.search_progress).setVisibility(View.GONE);
			if (response.getResponseCode() == ResponseCode.OK) {
				Assert.assertNotNull("With a ResponseCode.OK response, the adapter may not be null.",
						response.getData());
				hideError();
				
				if (simpleQuery != null && response.getData().getCount() == 1 && Isbn.initIsbn(simpleQuery) != null) {
					Item item = response.getData().getItem(0);
					Intent intent = new Intent(SearchResultsActivity.this, ItemDetailActivity.class);
					intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
					intent.putExtra(Constants.Intent.KEY_CALLING_INTENT, Constants.Intent.VALUE_SEARCH_RESULTS_ACTIVITY);
					startActivity(intent);
					
				} else 	if (simpleQuery != null && simpleQuery.length() == 0) {
					searchMessage.setText(R.string.label_enter_query);
					searchMessage.setVisibility(View.VISIBLE);
				
				} else if (simpleQuery != null && simpleQuery.length() <= 2) {
					// the simple search query was too short
					searchMessage.setText(R.string.query_too_short);
					searchMessage.setVisibility(View.VISIBLE);
					
				} else if (extendedQuery != null && extendedQuery.size() == 0) {
					// the extended search query was empty
					searchMessage.setText(R.string.query_empty_extended_search);
					searchMessage.setVisibility(View.VISIBLE);

					} else if (response.getData().getCount() == 0) {
					// the adapter is empty after search -> display 'no items found'
					searchMessage.setText(R.string.label_no_items_found);
					searchMessage.setVisibility(View.VISIBLE);
				}

				ItemAdapter adapter = response.getData();
				adapter.sort(comparator);
				searchResultsListView.setAdapter(adapter);
			} else {
				searchResultsListView.setAdapter(new ItemAdapter(SearchResultsActivity.this, new ArrayList<Item>()));
				displayError(response.getResponseCode().getErrorString(), reloadRunnable);
			}
		}
	};
}
