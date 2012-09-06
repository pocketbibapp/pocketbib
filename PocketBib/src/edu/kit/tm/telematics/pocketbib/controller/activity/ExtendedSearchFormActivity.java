package edu.kit.tm.telematics.pocketbib.controller.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.QueryParameters;

/**
 * This activity displays an extended search form, containg fields for all major
 * information fields.
 * 
 * When the user presses "Start search" the activity launches
 * {@link SearchResultsActivity} with {link {@link Constants#KEY_QUERY_TYPE} =
 * {@link Constants#VALUE_QUERY_TYPE_EXTENDED} and the query data in JSON-format
 * in {@link Constants#KEY_QUERY_DATA}.
 */
public class ExtendedSearchFormActivity extends BaseActivity {

	/** the label for the search types */
	private TextView typeLabel;

	/** EditText for the title */
	private EditText titleEditText;

	/**
	 * Spinner for selecting the search type (contaings or matches) of the title
	 */
	private Spinner titleSpinner;

	/** EditText for the author */
	private EditText authorEditText;

	/**
	 * Spinner for selecting the search type (contaings or matches) of the
	 * author
	 */
	private Spinner authorSpinner;

	/** ViewGroup containing all author related fields */
	private ViewGroup authorLayout;

	/** EditText for the publisher */
	private EditText publisherEditText;

	/**
	 * Spinner for selecting the search type (contaings or matches) of the
	 * publisher
	 */
	private Spinner publisherSpinner;

	/** ViewGroup containing all publisher related fields */
	private ViewGroup publisherLayout;

	/** EditText for the year */
	private EditText yearEditText;

	/** ViewGroup containing the year EditText */
	private ViewGroup yearLayout;

	/** EditText for the ISBN (not shown when magazines are being searched) */
	private EditText isbnEditText;

	/**
	 * ViewGroup containing the ISBN EditText (not shown when magazines are
	 * being searched)
	 */
	private ViewGroup isbnLayout;

	/** EditText for the ISSN (not shown when books are being searched) */
	private EditText issnEditText;

	/**
	 * ViewGroup containing the ISSN EditText (not shown when books are being
	 * searched)
	 */
	private ViewGroup issnLayout;

	/**
	 * Spinner for the availability options (don't care, lent only, available
	 * only)
	 */
	private Spinner availabilitySpinner;

	/**
	 * position of a search spinner, indicating that the field must only contain
	 * the term, not necessarily be equal to it
	 */
	private final static int POS_CONTAINS = 0;

	/**
	 * position of a search spinner, indicating that the field must equal the
	 * search term
	 */
	private final static int POS_EQUALS = 1;

	/**
	 * position of the availability spinner, indicating the search query doesn't
	 * include the availability
	 */
	private final static int POS_AVAILABILITY_DONT_CARE = 0;

	/**
	 * position of the availability spinner, indicating the search query
	 * requests available items only
	 */
	private final static int POS_AVAILABILITY_AVAILABLE = 1;

	/**
	 * position of the availability spinner, indicating the search query
	 * requests lent items only
	 */
	private final static int POS_AVAILABILITY_LENT = 2;

	/** tag for logging purposes */
	@SuppressWarnings("unused")
	private final static String TAG = "ExtendedSearchFormActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_extendedsearchform);
		setDisplayShowUp(WelcomeActivity.class, true);

		authorLayout = (ViewGroup) findViewById(R.id.layout_author);
		publisherLayout = (ViewGroup) findViewById(R.id.layout_publisher);
		yearLayout = (ViewGroup) findViewById(R.id.layout_year);
		isbnLayout = (ViewGroup) findViewById(R.id.layout_isbn);
		issnLayout = (ViewGroup) findViewById(R.id.layout_issn);

		titleEditText = (EditText) findViewById(R.id.input_title);
		authorEditText = (EditText) authorLayout.findViewById(R.id.input_author);
		publisherEditText = (EditText) publisherLayout.findViewById(R.id.input_publisher);
		yearEditText = (EditText) yearLayout.findViewById(R.id.input_year);
		isbnEditText = (EditText) isbnLayout.findViewById(R.id.input_isbn);
		issnEditText = (EditText) issnLayout.findViewById(R.id.input_issn);

		titleSpinner = (Spinner) findViewById(R.id.spinner_title);
		authorSpinner = (Spinner) authorLayout.findViewById(R.id.spinner_author);
		publisherSpinner = (Spinner) publisherLayout.findViewById(R.id.spinner_publisher);
		availabilitySpinner = (Spinner) findViewById(R.id.spinner_availability);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getSupportMenuInflater().inflate(R.menu.extended_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_start_extended_search:
			onSearchClick(null);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Returns the content of an EditText
	 * 
	 * @param editText
	 *            the EditText
	 * @return the content of an EditText
	 */
	private String readEditText(EditText editText) {
		return editText.getText().toString();
	}

	/**
	 * Returns the content of an EditText. If the Spinner is has
	 * {@link #POS_CONTAINS} selected, the content is wrapped in '{@code %} 
	 * '-wildcards
	 * 
	 * @param editText
	 *            the EditText
	 * @param spinner
	 *            the Spinner
	 * @return the content of an EditText
	 */
	private String readEditText(EditText editText, Spinner spinner) {
		assert spinner.getSelectedItemPosition() == POS_CONTAINS || spinner.getSelectedItemPosition() == POS_EQUALS;

		String text = editText.getText().toString().trim();

		if (text.length() > 0 && spinner.getSelectedItemPosition() == POS_CONTAINS) {
			text = "%" + text + "%";
		}

		return text;
	}

	/**
	 * Issues a search query.
	 * @param v the source View (may be null)
	 */
	public void onSearchClick(View v) {
		String title = readEditText(titleEditText, titleSpinner);
		String author = readEditText(authorEditText, authorSpinner);
		String publisher = readEditText(publisherEditText, publisherSpinner);
		String year = readEditText(yearEditText);
		String isbn = readEditText(isbnEditText);
		String issn = readEditText(issnEditText);

		Map<String, String> query = new HashMap<String, String>();

		if (title.length() > 0)
			query.put(QueryParameters.KEY_TITLE, title);

		if (author.length() > 0)
			query.put(QueryParameters.KEY_AUTHOR, author);

		if (publisher.length() > 0)
			query.put(QueryParameters.KEY_PUBLISHER, publisher);

		if (year.length() > 0)
			query.put(QueryParameters.KEY_PUBLICATION_YEAR, year);

		if (isbn.length() > 0)
			query.put(QueryParameters.KEY_ISBN, isbn);

		if (issn.length() > 0)
			query.put(QueryParameters.KEY_ISSN, issn);

		int availability = availabilitySpinner.getSelectedItemPosition();

		if (availability == POS_AVAILABILITY_AVAILABLE)
			query.put(QueryParameters.KEY_AVAILABLE, QueryParameters.VALUE_AVAILABILITY_AVAILABLE);
		else if (availability == POS_AVAILABILITY_LENT)
			query.put(QueryParameters.KEY_AVAILABLE, QueryParameters.VALUE_AVAILABILITY_LENT);
		else if (availability == POS_AVAILABILITY_DONT_CARE) {
			// NOP
		} else {
			throw new AssertionError("Illegal availability code: " + availability);
		}

		Intent intent = new Intent(this, SearchResultsActivity.class);
		intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.VALUE_QUERY_TYPE_EXTENDED);
		intent.putExtra(Constants.KEY_QUERY_DATA, new JSONObject(query).toString());

		startActivity(intent);
	}
}
