package edu.kit.tm.telematics.pocketbib.controller.activity;

import java.util.ArrayList;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.controller.activity.admin.ItemAddEditActivity;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.BibtexUtil;
import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopy;
import edu.kit.tm.telematics.pocketbib.model.library.ItemCopyAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.Magazine;
import edu.kit.tm.telematics.pocketbib.model.library.OtherItem;
import edu.kit.tm.telematics.pocketbib.model.library.managment.ItemCoverManager;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.model.user.UserUtil;
import edu.kit.tm.telematics.pocketbib.view.DialogUtil;

/**
 * The activity that shows the detailed information about a library item.
 * 
 * @see Item
 */
public class ItemDetailActivity extends BaseActivity {

	/** The item that this activity is showing information about. */
	private Item item;

	private Book amazonInfo;

	/** The cover image. */
	private ImageView coverImageView;

	/**
	 * The text that is overlaying the cover image. It gives the important
	 * information at a quick glance like the title.
	 */
	private TextView titleText, secondaryText;

	/** The total average rating score for this item. */
	private RatingBar averageRatingBar;

	/** The number of total ratings. */
	//private TextView ratingCountTextView;

	/**
	 * Gives the logged-in user the option to either borrow the library item or
	 * return it (if he is currently already borrowing it).
	 */
	private Button lendReturnButton;

	/** The description text. */
	private TextView descriptionTextView;

	/**
	 * All other metadata descriping this item.
	 */
	private TextView metadataTextView;

	/** Tag for logging purposes. */
	private final static String TAG = "ItemDetailActivity";

	private enum BorrowStatus {
		BORROW, RETURN, NOTAVAILABLE
	}

	/** Saves if the current user has already borrowed a copy of this item. */
	private BorrowStatus status = BorrowStatus.BORROW;

	private HorizontalScrollView titleScrollView;

	private Runnable titleScrollRunnable = new Runnable() {
		@Override
		public void run() {
			if (titleScrollRunnable != null) {
				titleScrollView.scrollBy(2, 0);
				titleScrollView.postDelayed(titleScrollRunnable, 30);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itemdetail);

		// the UP button normally navigates back to the WelcomeActivity except
		// if the calling intent was the SearchResultsActivity. In that case it
		// will lead back to it instead.
		if (Constants.Intent.VALUE_SEARCH_RESULTS_ACTIVITY.equals(getIntent().getStringExtra(
				Constants.Intent.KEY_CALLING_INTENT))) {
			setDisplayShowUp(SearchResultsActivity.class, true);
		} else {
			setDisplayShowUp(WelcomeActivity.class, true);
		}

		// this intent needs an item or else it will redirect back to the the
		// last activity
		if (savedInstanceState != null && savedInstanceState.containsKey(Constants.Intent.KEY_ITEM_PARCEL)) {
			item = savedInstanceState.getParcelable(Constants.Intent.KEY_ITEM_PARCEL);
		} else if (getIntent().hasExtra(Constants.Intent.KEY_ITEM_PARCEL)) {
			item = (Item) getIntent().getParcelableExtra(Constants.Intent.KEY_ITEM_PARCEL);
		}

		coverImageView = (ImageView) findViewById(R.id.cover_image);
		titleText = (TextView) findViewById(R.id.title);
		secondaryText = (TextView) findViewById(R.id.secondary);
		averageRatingBar = (RatingBar) findViewById(R.id.average_rating);
		//ratingCountTextView = (TextView) findViewById(R.id.rating_count);
		lendReturnButton = (Button) findViewById(R.id.lend_return);
		metadataTextView = (TextView) findViewById(R.id.metadata);
		descriptionTextView = (TextView) findViewById(R.id.description);
		titleScrollView = (HorizontalScrollView) findViewById(R.id.title_scroll);

		coverImageView.setImageDrawable(PocketBibApp.getItemCoverManager().getFallbackCover());

		Typeface robotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		titleText.setTypeface(robotoLight);
		secondaryText.setTypeface(robotoLight);

		titleScrollView.postDelayed(titleScrollRunnable, 2000);
		titleScrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				titleScrollRunnable = null;
				return false;
			}
		});

		if (item == null) {
			Log.w(TAG, "No item to show. Navigating back.");
			finish();
		} else {
			setItem(item);
			new LoadCoverAndInfoTask().execute(item);
		}

	}

	/**
	 * Sets all the relevant views to show information about the current item
	 * 
	 * @param item
	 *            the current item
	 */
	protected void setItem(Item item) {
		this.item = item;

		if (item == null) {
			Log.w(TAG, "No item to show. Navigating back.");
			finish();
		}

		// sets the rating bar for all ratings made
		averageRatingBar.setRating((item.getAverageRating() == null) ? null : item.getAverageRating().floatValue());
		//ratingCountTextView.setText("(" + item.getRatingCount() + ")");

		// sets the cover image overlay with general information
		titleText.setText(item.getTitle());

		if (TextUtils.isEmpty(item.getSecodaryInformation()))
			secondaryText.setVisibility(View.GONE);
		else
			secondaryText.setText(item.getSecodaryInformation());

		// sets the block with metadata
		CharSequence metadata = createMetadata();
		if (metadata.length() != 0) {
			metadataTextView.setText(metadata);
			metadataTextView.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_metadata).setVisibility(View.VISIBLE);
		} else {
			metadataTextView.setVisibility(View.GONE);
			findViewById(R.id.layout_metadata).setVisibility(View.GONE);
		}

		// sets the block with the description
		if (item.getDescription() != null) {
			descriptionTextView.setText(item.getDescription());
			descriptionTextView.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_description).setVisibility(View.VISIBLE);
		} else {
			descriptionTextView.setVisibility(View.GONE);
			findViewById(R.id.layout_description).setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(Constants.Intent.KEY_ITEM_PARCEL, item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		new ReloadInfo().execute(item);
	}

	/**
	 * Handles the click on the "all rating" TextView and starts the
	 * ItemRatingActivity to show all the ratings of this item.
	 * 
	 * @param v
	 *            the view
	 */
	public void onAllRatingClick(View v) {
		Intent intent = new Intent(this, ItemRatingActivity.class);
		intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.item_detail, menu);
		menu.findItem(R.id.menu_modify_item).setVisible(User.getCurrentUser().isAdministrator());
		return true;
	}

	/**
	 * Handles the click on the lendReturnButton.
	 * 
	 * If the user currently isn't logged in, he will get a message informing
	 * him to do so. If he is logged in and tries to borrow an item that has no
	 * available copies left, he will be given the option to inform another user
	 * already borrowing a copy of this item. Else he will either borrow or
	 * return a copy of this item depending on his current borrowing status.
	 * 
	 * @param v
	 *            the view
	 */
	public void onLendReturnClick(View v) {
		if (!(User.getCurrentUser() instanceof LoggedInUser)) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.dialog_not_logged_in_title);
			dialog.setMessage(R.string.label_welcome_help_login);
			dialog.setPositiveButton(R.string.dialog_action_login, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PocketBibApp.getRegistrationProvider().logout();
					startActivity(new Intent(ItemDetailActivity.this, LoginActivity.class));
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			dialog.create().show();

		} else if (status == BorrowStatus.NOTAVAILABLE) {
			new NoAvailableCopiesTask().execute();

		} else {
			new Thread(lendReturnRunnable).start();
		}

	}
	
	private void showLentReturnToast() {
		switch(status) {
		case BORROW:
			Toast.makeText(this, R.string.return_success, Toast.LENGTH_SHORT).show();
			break;
		case RETURN:
			Toast.makeText(this, R.string.borrow_success, Toast.LENGTH_SHORT).show();
			break;
		} 
	}

	/**
	 * Changes the text of the lendReturnButton depending on the current borrow
	 * status for this user.
	 */
	private void setLentReturnButtonText() {
		int buttonText = R.string.return_item;
		switch (status) {
		case BORROW:
			// the current user can borrow this item
			buttonText = R.string.borrow_item;
			break;
		case RETURN:
			// the current user is already borrowing and can return this item
			buttonText = R.string.return_item;
			break;
		case NOTAVAILABLE:
			// the item is unavailable at the current time
			buttonText = R.string.not_available;
		}
		lendReturnButton.setText(buttonText);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.menu_modify_item:
			Intent intent = new Intent(this, ItemAddEditActivity.class);
			intent.putExtra(Constants.Intent.KEY_ADD_EDIT_MODE, Constants.Intent.VALUE_EDIT_MODE);
			intent.putExtra(Constants.Intent.KEY_ITEM_PARCEL, item);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(menuItem);
		}

	}

	/**
	 * Open up a browser to show the amazon page for the current item
	 * 
	 * @param v
	 */
	public void onBuyClick(View v) {
		Uri uri = Uri.parse(amazonInfo.getDetailUrl());
		startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}
	
	public void onExportBibtexClick(View v) {
		String bibTex = BibtexUtil.getBibtex(item);
		
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "BibTeX");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, bibTex);
		startActivity(Intent.createChooser(sharingIntent, getString(R.string.bibtex_share_title)));
	}

	/**
	 * Returns all the additional metadata formatted so that it can be shown on
	 * screen.
	 * 
	 * @return the formatted metadata
	 */
	private CharSequence createMetadata() {
		SpannableStringBuilder builder = new SpannableStringBuilder();

		if (item instanceof Book) {
			Book b = (Book) item;
			if (b.getEdition() != null && b.getEdition().length() != 0)
				builder.append(formatMetadata(R.string.item_edition, b.getEdition()));
			if (b.getIsbn() != null)
				builder.append(formatMetadata(R.string.item_isbn, b.getIsbn().getReadableIsbn13()));
			if (b.getPageCount() != Constants.NOT_SAVED && b.getPageCount() != 0)
				builder.append(formatMetadata(R.string.item_pageCount, Integer.toString(b.getPageCount())));
			if (b.getPosition() != null && b.getPosition().length() != 0)
				builder.append(formatMetadata(R.string.item_position, b.getPosition()));
			if (b.getPublicationYear() != Constants.NOT_SAVED)
				builder.append(formatMetadata(R.string.item_publicationYear, Integer.toString(b.getPublicationYear())));
			if (b.getPublisher() != null && b.getPublisher().length() != 0)
				builder.append(formatMetadata(R.string.item_publisher, b.getPublisher()));
		} else if (item instanceof Magazine) {
			Magazine m = (Magazine) item;
			if (m.getEdition() != null && m.getEdition().length() != 0)
				builder.append(formatMetadata(R.string.item_edition, m.getEdition()));
			if (m.getIssn() != null)
				builder.append(formatMetadata(R.string.item_issn, m.getIssn().getReadableIssn()));
			if (m.getPageCount() != Constants.NOT_SAVED && m.getPageCount() != 0)
				builder.append(formatMetadata(R.string.item_pageCount, Integer.toString(m.getPageCount())));
			if (m.getPosition() != null && m.getPosition().length() != 0)
				builder.append(formatMetadata(R.string.item_position, m.getPosition()));
			if (m.getPublicationDate() != null)
				builder.append(formatMetadata(R.string.item_publicationDate, m.getPublicationDate().toString()));
			if (m.getPublisher() != null && m.getPublisher().length() != 0)
				builder.append(formatMetadata(R.string.item_publisher, m.getPublisher()));

		} else if (item instanceof OtherItem) {
			OtherItem other = (OtherItem) item;
			if (other.getAuthor() != null && other.getAuthor().length() != 0
					&& !other.getAuthor().equals(other.getSecodaryInformation()))
				builder.append(formatMetadata(R.string.item_author, other.getAuthor()));
			if (other.getEdition() != null && other.getEdition().length() != 0)
				builder.append(formatMetadata(R.string.item_edition, other.getEdition()));
			if (other.getIsbn() != null)
				builder.append(formatMetadata(R.string.item_isbn, other.getIsbn().getReadableIsbn13()));
			if (other.getIssn() != null)
				builder.append(formatMetadata(R.string.item_issn, other.getIssn().getReadableIssn()));
			if (other.getPageCount() != Constants.NOT_SAVED && other.getPageCount() != 0)
				builder.append(formatMetadata(R.string.item_pageCount, Integer.toString(other.getPageCount())));
			if (other.getPosition() != null && other.getPosition().length() != 0)
				builder.append(formatMetadata(R.string.item_position, other.getPosition()));
			if (other.getPublicationDate() != null)
				builder.append(formatMetadata(R.string.item_publicationDate, other.getPublicationDate().toString()));
			if (other.getPublicationYear() != Constants.NOT_SAVED)
				builder.append(formatMetadata(R.string.item_publicationYear,
						Integer.toString(other.getPublicationYear())));
			if (other.getPublisher() != null && other.getPublisher().length() != 0)
				builder.append(formatMetadata(R.string.item_publisher, other.getPublisher()));
		} else {
			Assert.fail("Can't create metadata for this subclass of item: " + item.getClass().getName());
		}
		return (builder.length() > 1) ? builder.subSequence(1, builder.length()) : builder;
	}

	/**
	 * Returns a single line of formatted metadata
	 * 
	 * @param labelStrinRes
	 *            the android resource ID for the attribute name
	 * @param text
	 *            the string representation of the attribute
	 * @return the formatted metadata
	 */
	private SpannableStringBuilder formatMetadata(int labelStrinRes, String text) {
		if (text == null || "".equals(text) || "-1".equals(text)) {
			return new SpannableStringBuilder("");
		} else {
			StyleSpan style = new StyleSpan(Typeface.BOLD);
			String label = getString(labelStrinRes) + ": ";
			SpannableStringBuilder span = new SpannableStringBuilder("\n" + label + text);
			span.setSpan(style, 0, label.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
			return span;
		}
	}

	/**
	 * A Runnable that checks if the logged-in user is already borrowing a copy
	 * of this item and sets the lendReturnButton accordingly.
	 */
	private final ReloadRunnable lendStatusRunnable = new ReloadTask<ItemAdapter>() {

		@Override
		public Response<ItemAdapter> doInBackground() {
			User user = User.getCurrentUser();
			if (user instanceof LoggedInUser) {
				return PocketBibApp.getLibraryManager().getBorrowedItems((LoggedInUser) user);
			} else {
				return new Response<ItemAdapter>(ResponseCode.OK, new ItemAdapter(ItemDetailActivity.this,
						new ArrayList<Item>()));
			}
		}

		public void onPostExecute(Response<ItemAdapter> response) {
			status = item.getAvailableCopies() == 0 ? BorrowStatus.NOTAVAILABLE : BorrowStatus.BORROW;
			if (response.getResponseCode() == ResponseCode.OK) {
				for (Item borrowedItem : response.getData()) {
					if (item.getItemId().equals(borrowedItem.getItemId())) {
						// the user is already borrowing one copy
						status = BorrowStatus.RETURN;
					}
				}

			} else {
				DialogUtil.showErrorDialog(ItemDetailActivity.this, response.getResponseCode().getErrorStringRes());
			}

			setLentReturnButtonText();
		};
	};

	/**
	 * A Runnable that either borrows or returns a copy of this item depending
	 * on the the borrowing status. If the operation was a success it will also
	 * change the text of the lendReturnButton automatically.
	 */
	private final ReloadRunnable lendReturnRunnable = new ReloadRunnable() {
		@Override
		public boolean execute() {
			final ResponseCode responseCode;
			if (status == BorrowStatus.BORROW) {
				responseCode = PocketBibApp.getLibraryManager().borrowItem(item);
			} else {
				responseCode = PocketBibApp.getLibraryManager().returnItem(item);
			}

			if (responseCode == ResponseCode.OK) {
				status = status == BorrowStatus.BORROW ? BorrowStatus.RETURN : BorrowStatus.BORROW;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showLentReturnToast();
						setLentReturnButtonText();
					}
				});
			} else {
				DialogUtil.showErrorDialog(ItemDetailActivity.this, responseCode.getErrorStringRes());
			}

			return responseCode == ResponseCode.OK;
		}
	};

	/**
	 * Loads a cover from the item cover manager. If the manager returns no
	 * cover, the ItemDetailActivity will just leave the fallback cover showing.
	 * Also loads the online price information if this object is a book.
	 * 
	 * @see ItemCoverManager
	 */
	private class LoadCoverAndInfoTask extends AsyncTask<Item, Void, Void> {

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Void doInBackground(Item... items) {
			if (items.length == 0)
				return null;

			Item item = items[0];
			if (item.getCover() == null)
				item.setCover(PocketBibApp.getItemCoverManager().getCover(item));
			if (item instanceof Book)
				amazonInfo = PocketBibApp.getItemInformationManager().getBook(((Book) item).getIsbn());

			return null;
		}

		@Override
		protected void onCancelled() {
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPostExecute(Void result) {
			setSupportProgressBarIndeterminateVisibility(false);

			if (item.getCover() != null)
				coverImageView.setImageDrawable(item.getCover());
			if (amazonInfo != null && amazonInfo.getPrice() != null && amazonInfo.getPrice() > 0) {
				TextView buy = (TextView) findViewById(R.id.item_buy);
				String price = getString(R.string.item_price) + " (" + String.valueOf(amazonInfo.getPrice() / 100)
						+ "," + String.valueOf(((amazonInfo.getPrice() % 100) < 10) ? "0" : "")
						+ String.valueOf(amazonInfo.getPrice() % 100) + " \u20AC)";
				buy.setText(price);
				buy.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Reloads info after resume
	 */
	private class ReloadInfo extends AsyncTask<Item, Void, Response<Item>> {
		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Response<Item> doInBackground(Item... items) {
			if(items == null || items.length == 0 || items[0] == null)
				finish();
			
			Item i = items[0];
			Response<Item> response = PocketBibApp.getLibraryManager().getItem(i.getItemId());
			
			if(response.getResponseCode() == ResponseCode.OK) {
				lendStatusRunnable.run();
			}
			
			return response;
		}

		@Override
		protected void onCancelled() {
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPostExecute(Response<Item> response) {
			setSupportProgressBarIndeterminateVisibility(false);

			if (response.getResponseCode() == ResponseCode.OK) {
				setItem(response.getData());
			} else {
				Log.e(TAG, response.getResponseCode().getErrorString());
				finish();
			}
		}
	}

	/**
	 * Loads the list of users that are borrowing a copy of this item and sets
	 * the ItemCopyAdapter as the adapter for the provided ListView. This is
	 * used so that the user can pick another user to inform him about his
	 * desire to borrow this item.
	 */
	private class NoAvailableCopiesTask extends AsyncTask<Void, Void, Response<ItemCopyAdapter>> {

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Response<ItemCopyAdapter> doInBackground(Void... items) {
			return PocketBibApp.getLibraryManager().getItemCopies(item.getItemId());
		}

		@Override
		protected void onCancelled() {
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPostExecute(Response<ItemCopyAdapter> response) {
			setSupportProgressBarIndeterminateVisibility(false);

			if (response.getResponseCode() != ResponseCode.OK) {
				DialogUtil.showErrorDialog(ItemDetailActivity.this, response.getResponseCode().getErrorStringRes());
			} else if (response.getData().getCount() == 0) {
				Log.w(TAG, "Copy list length is 0 for " + item);
			} else {
				final Dialog dialog = new Dialog(ItemDetailActivity.this);
				dialog.setTitle(R.string.contact_user);
				ListView copiesList = new ListView(ItemDetailActivity.this);
				ItemCopyAdapter copies = response.getData();
				copiesList.setAdapter(copies);
				copiesList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						dialog.cancel();
						ItemCopy copy = (ItemCopy) parent.getItemAtPosition(position);
						if (copy.getUser() != null && item != null) {
							UserUtil.emailUser(ItemDetailActivity.this, copy.getUser(),
									UserUtil.EmailType.ITEM_RETURN_REQUEST, item);
						} else {
							Log.e(TAG, "Error in onCopiesClick: " + copy + ", " + item);
						}

					}
				});

				dialog.setContentView(copiesList);

				if (copies != null && copies.getCount() > 0) {
					dialog.show();
				}
				Log.w(TAG, "no available copies left: " + copies);
			}

		}
	}

}
