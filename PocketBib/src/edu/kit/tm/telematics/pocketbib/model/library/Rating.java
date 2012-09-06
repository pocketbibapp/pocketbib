package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.RatingConstants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;
import edu.kit.tm.telematics.pocketbib.model.user.User;
import edu.kit.tm.telematics.pocketbib.model.user.User.NotLoggedInException;

/**
 * A rating with optional comment.
 * 
 * @param <T> the type of Item to be rated (must be either Book, Magazine or OtherItem)
 */
public class Rating<T extends Item> {

	/**
	 * the rating id (or {@code Constants.ID_NOT_SAVED} when not stored in
	 * database)
	 */
	private Integer ratingId = Constants.ID_NOT_SAVED;

	/** the user that submitted the rating */
	private LoggedInUser user = null;

	/** the rated item */
	private T item = null;

	/** the rating (from 1 to 5 stars) */
	private Integer rating = null;

	/** the comment (optional) */
	private String comment = null;

	/** the creation time */
	private Date creationTime = null;

	/** the maximum rating */
	public static final int MAX = 5;
	
	/** the minimum rating */
	public static final int MIN = 1;
	
	/**
	 * Creates a new Rating.
	 */
	private Rating() {
	}

	/**
	 * Prepares the Rating object to be filled with information from the
	 * database.
	 * 
	 * @param ratingId
	 *            the id
	 * @param user
	 *            the LoggedInUser who submitted the rating
	 * @param item
	 *            the rated Item
	 * @param creationTime
	 *            the creation time of the rating
	 * @return the Rating {@code this}
	 */
	public static <T extends Item> Rating<T> instantiateExisting(int ratingId, LoggedInUser user, T item,
			Date creationTime) {
		return new Rating<T>().setRatingId(ratingId).setUser(user).setItem(item).setCreationTime(creationTime);
	}

	/**
	 * Prepares a new Rating object. The rating's creation time is set to the
	 * current time and the user is set to the current logged in user.
	 * 
	 * @return the Rating {@code this}
	 * @throws NotLoggedInException
	 *             - if the current user is not logged in.
	 */
	public static <T extends Item> Rating<T> createNew(T item) throws NotLoggedInException {
		User user = User.getCurrentUser();

		if (user instanceof LoggedInUser) {
			return new Rating<T>().setCreationTime(new Date(System.currentTimeMillis()))
					.setUser((LoggedInUser) user).setItem(item);
		} else {
			throw new User.NotLoggedInException();
		}
	}

	/**
	 * Sets the id.
	 * 
	 * @param ratingId
	 *            the id
	 * @return the Rating {@code this}
	 */
	private Rating<T> setRatingId(int ratingId) {
		this.ratingId = ratingId;
		return this;
	}

	/**
	 * Returns the rating.
	 * 
	 * @return the rating
	 */
	public Integer getRating() {
		return rating;
	}

	/**
	 * Sets the rating.
	 * 
	 * @param rating
	 *            the rating
	 * @return the Rating {@code this}
	 */
	public Rating<T> setRating(Integer rating) {
		this.rating = rating;
		return this;
	}

	/**
	 * Returns the comment.
	 * 
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment.
	 * 
	 * @param comment
	 *            the comment
	 * @return the Rating {@code this}
	 */
	public Rating<T> setComment(String comment) {
		this.comment = comment;
		return this;
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public int getRatingId() {
		return ratingId;
	}

	/**
	 * Returns the user.
	 * 
	 * @return the user
	 */
	public LoggedInUser getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user
	 *            the user
	 * @return the Rating {@code this}
	 */
	private Rating<T> setUser(LoggedInUser user) {
		this.user = user;
		return this;
	}

	/**
	 * Sets the item.
	 * 
	 * @param item
	 *            the item
	 * @return the Rating {@code this}
	 */
	private Rating<T> setItem(T item) {
		this.item = item;
		return this;
	}

	/**
	 * Returns the rated item.
	 * 
	 * @return the rated item.
	 */
	public T getItem() {
		return item;
	}

	/**
	 * Sets the creation time
	 * 
	 * @param creationTime
	 *            the creation time
	 * @return the Rating {@code this}
	 */
	private Rating<T> setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
		return this;
	}

	/**
	 * Returns the creation time
	 * 
	 * @return the creation time
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns true, if the rating is stored in the database
	 * 
	 * @return true, if the rating is stored in the database
	 */
	public boolean isStoredInDatabase() {
		return ratingId != Constants.ID_NOT_SAVED;
	}

	/**
	 * Saves the rating in the database by either updating an existing row or
	 * inserting a new row.
	 * 
	 * @return true, if the saving succeeds, false on error
	 */
	public ResponseCode save() {
		Response<Integer> response = PocketBibApp.getLibraryManager().insertOrUpdateRating(this);

		if (response.getResponseCode() == ResponseCode.OK) {
			this.ratingId = response.getData();
		}
		return response.getResponseCode();
	}

	/**
	 * Removes the rating from the database.
	 * 
	 * @return true, if the removal succeeds, false on error
	 */
	public ResponseCode remove() {
		if (!isStoredInDatabase()) {
			// Rating is not stored in the database - no deletion required
			return ResponseCode.OK;
		}

		ResponseCode responseCode = PocketBibApp.getLibraryManager().removeRating(this);

		if (responseCode == ResponseCode.OK) {
			this.ratingId = Constants.ID_NOT_SAVED;
		}
		
		return responseCode;
	}

	@Override
	public boolean equals(Object o) {
		if(ratingId == null || ratingId.equals(Constants.ID_NOT_SAVED)) {
			return false;
		} else if (o instanceof Rating<?>) {
			return ratingId.equals(((Rating<?>) o).ratingId);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return ratingId;
	}

	@Override
	public String toString() {
		return "Rating [id=" + ratingId + " rating=" + rating
				+ " user=" + (user != null ? user.toString() : "null")
				+ " item=" + (item != null ? item.toString() : "null");
	}
	
	/**
	 * Returns the object as a {@link JSONObject}.
	 * @return the object as a {@link JSONObject}
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			json.put(RatingConstants.RATING__ID, getRatingId());
			json.put(RatingConstants.USER_ID, (getUser() == null) ? null : getUser().getUserId());
			json.put(RatingConstants.ITEM_ID, (getItem() == null) ? null : getItem().getItemId());
			json.put(RatingConstants.RATING, getRating());
			json.putOpt(RatingConstants.COMMENT, getComment());
//			json.put(RatingConstants.CREATION_TIME, (creationTime == null) ? null : creationTime.toGMTString());
		} catch (JSONException e) {
			e.printStackTrace();
			json = null;
		}
		return json;
	}
}
