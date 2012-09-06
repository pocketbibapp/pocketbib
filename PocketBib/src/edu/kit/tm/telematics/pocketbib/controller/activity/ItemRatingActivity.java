package edu.kit.tm.telematics.pocketbib.controller.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.library.Item;
import edu.kit.tm.telematics.pocketbib.model.library.ItemAdapter;
import edu.kit.tm.telematics.pocketbib.model.library.Rating;
import edu.kit.tm.telematics.pocketbib.model.library.RatingAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.model.user.User.NotLoggedInException;
import edu.kit.tm.telematics.pocketbib.view.DialogUtil;

/**
 * This activity shows all the ratings for a item and gives the current user an
 * option to add or modify his rating, too.
 */
public class ItemRatingActivity extends BaseActivity {

	/** Tag for logging purposes. */
	private final static String TAG = "ItemRatingActivity";

	/** The item of this activity. */
	private Item item;

	/** The ListView with all the ratings. */
	private ListView itemRatings;

	/** The rating of the current user. */
	private Rating<? extends Item> myRating;
	
	/** the AlertDialog for UnitTesting purposes */
	private WeakReference<AlertDialog> ratingDialog;
	
	private ListView myRatingListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.activity_itemrating);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		itemRatings = (ListView) findViewById(R.id.item_ratings);
		myRatingListView = (ListView) findViewById(R.id.own_rating);
		
		itemRatings.setEmptyView(findViewById(R.id.rating_no_items));
		myRatingListView.setEmptyView(findViewById(R.id.no_own_rating));
		myRatingListView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onOwnRatingClick(view);
			}
		});

		Item item = (Item) getIntent().getParcelableExtra(Constants.Intent.KEY_ITEM_PARCEL);		
		
		if(item != null) {
			setItem(item);
		} else {
			Log.w(TAG, "No item to show. Navigating back.");
			finish();
		}
		
	}
	
	/**
	 * Sets to show the ratings of the current item
	 * @param item the current item 
	 */
	public void setItem(Item item) {
		this.item = item;
		Assert.assertNotNull(item);
		new Thread(reloadRunnable).start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (Constants.API_LEVEL >= 11) {
			switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
		
	}

	/**
	 * Handles the click to show the rating for the current user
	 * 
	 * @param v
	 *            the view calling this method
	 */
	public void onOwnRatingClick(View v) {
		if (!(User.getCurrentUser() instanceof LoggedInUser)) {
			// The user is not logged in
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.dialog_not_logged_in_title);
			dialog.setMessage(R.string.label_welcome_help_login);
			dialog.setPositiveButton(R.string.dialog_action_login, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PocketBibApp.getRegistrationProvider().logout();
					startActivity(new Intent(ItemRatingActivity.this, LoginActivity.class));
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			dialog.create().show();
			return;
		}

		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle(R.string.rating_dialog_title);
		View group = getLayoutInflater().inflate(R.layout.own_rating, null);
		final RatingBar ratingBar = (RatingBar) group.findViewById(R.id.own_rating);
		final EditText comment = (EditText) group.findViewById(R.id.own_comment);
		helpBuilder.setView(group);

		if (myRating != null && myRating.getRating() != null)
			ratingBar.setRating(myRating.getRating());
		if (myRating != null && myRating.getComment() != null && myRating.getComment().length() != 0)
			comment.setText(myRating.getComment());

		helpBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (myRating == null) {
					try {
						myRating = Rating.createNew(item);
					} catch (NotLoggedInException e) {
						DialogUtil.showErrorDialog(ItemRatingActivity.this, R.string.label_welcome_help_login);
					}
				}

				if (myRating != null) {
					Log.d(TAG, "adding rating: " + myRating.toString());
					myRating.setRating((int) ratingBar.getRating());
					myRating.setComment(comment.getText().toString());

					new Thread(submitRatingRunnable).start();
				}
			}
		});

		helpBuilder.setNegativeButton(android.R.string.cancel, null);

		ratingDialog = new WeakReference<AlertDialog>(helpBuilder.create());
		
		if (ratingDialog != null)
			ratingDialog.get().show();
	}
	
	public AlertDialog getRatingDialog() {
		return ratingDialog.get();
	}

	/**
	 * A Runnable that loads all the comments for the current item and shows
	 * them.
	 */
	private final ReloadRunnable reloadRunnable = new ReloadTask<RatingAdapter>() {

		@Override
		public Response<RatingAdapter> doInBackground() {
			if (item != null) {
				return PocketBibApp.getLibraryManager().getItemRatings(item.getItemId());
			} else {
				return new Response<RatingAdapter>(ResponseCode.OK, new RatingAdapter(ItemRatingActivity.this,
						new ArrayList<Rating<? extends Item>>()));
			}
			
		}
		
		public void onPostExecute(Response<RatingAdapter> response) {
			if (response.getResponseCode() == ResponseCode.OK) {
				RatingAdapter ratings = response.getData();
				if ((User.getCurrentUser() instanceof LoggedInUser)) {
					String userId = Integer.toString(((LoggedInUser) User.getCurrentUser()).getUserId());
					ratings.getFilter().filter(userId);
				}
				itemRatings.setAdapter(ratings);
				((TextView) findViewById(R.id.total_ratings)).setText(getString(R.string.total_ratings, ratings.getCount()));

				if (myRating == null) {
					for (Rating<? extends Item> rating : ratings) {
						if (User.getCurrentUser().equals(rating.getUser())) {
							myRating = rating;
							List<Rating<? extends Item>> itemCollection = new ArrayList<Rating<? extends Item>>();
							itemCollection.add(myRating);
							myRatingListView.setAdapter(new RatingAdapter(ItemRatingActivity.this, itemCollection));
							((TextView) findViewById(R.id.own_rating_title)).setText(R.string.own_rating_exists);
							((TextView) findViewById(R.id.total_ratings)).setText(getString(R.string.total_ratings, ratings.getCount() - 1));
						}
					}
				}
			} else {
				itemRatings.setAdapter(null);
				displayError(response.getResponseCode().getErrorString(), reloadRunnable);
			}
		};
	};

	/**
	 * A runnable that submits the rating and comment saved under myRating to
	 * the server.
	 */
	private final ReloadRunnable submitRatingRunnable = new ReloadRunnable() {
		
		@Override
		public boolean execute() {
			final ResponseCode responseCode;
			if (myRating == null) {
				return false;
			} else if (myRating.getRating() == null) {
				DialogUtil.showErrorDialog(ItemRatingActivity.this, R.string.dialog_own_rating_missing);
				return false;
			} else {
				Log.i(TAG, "Submitting rating: " + myRating.toString());
				responseCode = myRating.save();
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (responseCode != ResponseCode.OK) {


						AlertDialog.Builder helpBuilder = new AlertDialog.Builder(ItemRatingActivity.this);
						helpBuilder.setTitle(R.string.error_dialog_heading);
						helpBuilder.setMessage(responseCode.getErrorString());
						helpBuilder.setNeutralButton(R.string.reload, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new Thread(submitRatingRunnable).start();
							}
						});
						helpBuilder.setPositiveButton(android.R.string.ok, null);
						helpBuilder.show();

					} else {
						Toast.makeText(ItemRatingActivity.this, R.string.dialog_success_message, Toast.LENGTH_SHORT).show();
						finish();
					}
				}
			}
					);

			return responseCode == ResponseCode.OK;
		}
	};
}
