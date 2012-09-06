package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.Date;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants;
import edu.kit.tm.telematics.pocketbib.model.Response;
import edu.kit.tm.telematics.pocketbib.model.ResponseCode;

/**
 * A basic library item.
 */
public abstract class Item implements Parcelable {

	private final int NO_PAGE_COUNT = 0;
	
	/**
	 * the item id (or {@code Constants.ID_NOT_SAVED} when not stored in
	 * database)
	 */
	protected int itemId = Constants.ID_NOT_SAVED;

	/** the title */
	protected String title = null;

	/** the description */
	protected String description = null;

	/** the sum of all the ratings */
	protected Double sumRating = null;

	/** the number of ratings */
	protected int ratingCount = 0;

	/** the number of available (not lent) copies */
	protected int availableCopies = 0;

	/** the position inside the library */
	protected String position = null;

	/** the edition */
	protected String edition = null;

	/** the number of pages */
	protected int pageCount = 0;

	/** if the item is currently activated */
	protected boolean isActive = true;

	/** the publisher */
	protected String publisher = null;

	/** the cover drawable */
	protected Drawable cover = null;

	/** the thumbnail drawable */
	protected Drawable thumbnail = null;
	
	/** The price of the item */
	protected int price = Constants.NOT_SAVED;
	
	/** The url to an online shop for the item */
	protected String detailUrl = null;

	/**
	 * Creates a new item with no ID set.
	 */
	protected Item() {
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Item) {
			Item item = (Item) o;
			return itemId != Constants.ID_NOT_SAVED && item.itemId == itemId;
		} else {
			return false;
		}
	}

	/**
	 * Returns the number of available copies.
	 * @return the number of available copies
	 */
	public int getAvailableCopies() {
		return availableCopies;
	}

	/**
	 * Returns the rating average.
	 * @return the rating average
	 */
	public Double getAverageRating() {
		if(sumRating == null || ratingCount == 0) {
			return Double.valueOf(0.0);
		} else {
			return sumRating / ratingCount;
		}
	}

	/**
	 * Returns the sum of all ratings.
	 * @return the sum of all ratings
	 */
	public Double getSumRating() {
		return sumRating;
	}

	/**
	 * Sets the sum of all ratings.
	 * @param sumRating the sum of all ratings
	 * @return the current item
	 */
	public Item setSumRating(Double sumRating) {
		this.sumRating = sumRating;
		return this;
	}

	/**
	 * Sets if the item is activated.
	 * @param active <code>true</code> if is active; <code>false</code> if not
	 * @return the current item
	 */
	public Item setActive(boolean active) {
		this.isActive = active;
		return this;
	}

	/**
	 * Returns if the item is activated.
	 * @return if the item is activated
	 */
	public boolean getActive() {
		return isActive;
	}

	/**
	 * Returns the cover drawable.
	 * 
	 * @return the cover drawable
	 */
	public Drawable getCover() {
		return cover;
	}

	/**
	 * Returns the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the edition.
	 * 
	 * @return the edition
	 */
	public String getEdition() {
		return edition;
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id.
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * Returns the page count.
	 * 
	 * @return the page count
	 */
	public int getPageCount() {
		if(pageCount == NO_PAGE_COUNT || pageCount < 0) {
			return NO_PAGE_COUNT;
		} else {
			return pageCount;
		}
		
	}

	/**
	 * Returns the position.
	 * 
	 * @return the position
	 */
	public String getPosition() {
		return position;
	}

	/**
	 * Returns the publisher.
	 * 
	 * @return the publisher
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * Returns the number of ratings
	 * 
	 * @return the number of ratings
	 */
	public int getRatingCount() {
		return ratingCount;
	}

	/**
	 * Returns secondary information about the item. "Secondary information" is
	 * displayed in a second row below the title in the search results for each
	 * item.
	 * 
	 * @return secondary information about the item
	 */
	public abstract String getSecodaryInformation();

	/**
	 * Returns the thumbnail.
	 * 
	 * @return the thumbnail
	 */
	public Drawable getThumbnail() {
		return thumbnail;
	}

	/**
	 * Returns the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	@Override
	public int hashCode() {
		return itemId;
	}

	/**
	 * Returns true, if the item is already stored in the database.
	 * 
	 * @return true, if the item is already stored in the database.
	 */
	public boolean isStoredInDatabase() {
		return itemId != Constants.ID_NOT_SAVED;
	}

	/**
	 * Removes the item from the database.
	 * 
	 * @return true, if the removal succeeds, false on error
	 */
	public ResponseCode remove() {
		if (!isStoredInDatabase()) {
			// Item is not stored in the database - no deletion required
			return ResponseCode.OK;
		}

		ResponseCode responseCode = PocketBibApp.getLibraryManager().removeItem(this);

		if (responseCode == ResponseCode.OK) {
			this.itemId = Constants.ID_NOT_SAVED;
		}

		return responseCode;
	}

	/**
	 * Saves the item to the database by either inserting or updating.
	 * 
	 * @return true, if the saving succeeds, false on error.
	 */
	public ResponseCode save() {
		Response<Integer> response = PocketBibApp.getLibraryManager().insertOrUpdateItem(this);
		ResponseCode responseCode = response.getResponseCode();

		if (responseCode == ResponseCode.OK) {
			this.itemId = response.getData();
		}

		return responseCode;
	}

	/**
	 * Sets the number of available copies.
	 * 
	 * @param availableCopies
	 *            the number of available copies
	 * @return the item ({@code this})
	 */
	public Item setAvailableCopies(int availableCopies) {
		this.availableCopies = availableCopies;
		return this;
	}

	/**
	 * Sets the cover drawable.
	 * 
	 * @param cover
	 *            the cover drawable
	 * @return the item ({@code this})
	 */
	public Item setCover(Drawable cover) {
		this.cover = cover;
		return this;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            the description
	 * @return the item ({@code this})
	 */
	public Item setDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Sets the edition.
	 * 
	 * @param edition
	 *            the edition
	 * @return the item ({@code this})
	 */
	public Item setEdition(String edition) {
		this.edition = edition;
		return this;
	}

	/**
	 * Sets the item id.
	 * 
	 * @param itemId
	 *            the item id.
	 */
	public Item setItemId(int itemId) {
		this.itemId = itemId;
		return this;
	}

	/**
	 * Sets the page count.
	 * 
	 * @param pageCount
	 *            the page count
	 * @return the item ({@code this})
	 */
	public Item setPageCount(Integer pageCount) {
		if(pageCount == null) {
			this.pageCount = NO_PAGE_COUNT;
		} else {
			this.pageCount = pageCount;			
		}
		
		return this;
	}

	/**
	 * Sets the position.
	 * 
	 * @param position
	 *            the position
	 * @return the item ({@code this})
	 */
	public Item setPosition(String position) {
		this.position = position;
		return this;
	}

	/**
	 * Sets the publisher.
	 * 
	 * @param publisher
	 *            the publisher.
	 * @return the item ({@code this})
	 */
	public Item setPublisher(String publisher) {
		this.publisher = publisher;
		return this;
	}

	/**
	 * Sets the number of ratings.
	 * 
	 * @param ratingCount
	 *            the number of ratings.
	 * @return the item ({@code this})
	 */
	public Item setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
		return this;
	}

	/**
	 * Sets the thumbnail.
	 * 
	 * @param thumbnail
	 *            the thumbnail
	 * @return the item ({@code this})
	 */
	public Item setThumbnail(Drawable thumbnail) {
		this.thumbnail = thumbnail;
		return this;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the title
	 * @return the item ({@code this})
	 */
	public Item setTitle(String title) {
		this.title = title;
		return this;
	}
	
	/**
	 * Returns the price
	 * @return the price
	 */
	public Integer getPrice() {
		return price;
	}

	/**
	 * Sets the price.
	 * @param price the price
	 * @return this item
	 */
	public Item setPrice(Integer price) {
		this.price = price;
		return this;
	}
	
	/**
	 * Returns the price
	 * @return the price
	 */
	public String getDetailUrl() {
		return detailUrl;
	}

	/**
	 * Sets the price.
	 * @param price the price
	 * @return this item
	 */
	public Item setDetailUrl(String url) {
		this.detailUrl = url;
		return this;
	}

	@Override
	public String toString() {
		return "Item [id=" + itemId + " title=" + title + "]";
	}

	/**
	 * Creates a {@link Bundle} with all the attributes of the current item
	 * 
	 * @return the bundle
	 * @see ItemConstants
	 */
	public Bundle writeToBundle() {
		Bundle b = new Bundle();
		
		b.putString(ItemConstants.TITLE, title);
		b.putString(ItemConstants.DESCRIPTION, description);
		b.putInt(ItemConstants.RATING_COUNT, ratingCount);
		b.putInt(ItemConstants.AVAILABLE_COPIES, availableCopies);
		b.putString(ItemConstants.POSITION, position);
		b.putString(ItemConstants.EDITION, edition);
		b.putInt(ItemConstants.PAGE_COUNT, pageCount);
		b.putString(ItemConstants.DETAIL_URL, detailUrl);
		b.putString(ItemConstants.IS_ACTIVE, isActive ? "true" : "false");
		b.putString(ItemConstants.PUBLISHER, publisher);
		
		if (itemId != Constants.ID_NOT_SAVED)
			b.putInt(ItemConstants.ITEM_ID, itemId);
		
		if (price != Constants.NOT_SAVED)
			b.putInt(ItemConstants.PRICE, price);
		
		if (sumRating != null)
			b.putDouble(ItemConstants.SUM_RATING, sumRating.doubleValue());
		
		return b;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}
	
	protected void setValuesFromBundle(Bundle bundle) {
		setItemId(bundle.getInt(ItemConstants.ITEM_ID, Constants.ID_NOT_SAVED));
		setTitle(bundle.getString(ItemConstants.TITLE));
		setDescription(bundle.getString(ItemConstants.DESCRIPTION));
		setSumRating(bundle.getDouble(ItemConstants.SUM_RATING));
		setRatingCount(bundle.getInt(ItemConstants.RATING_COUNT));
		setAvailableCopies(bundle.getInt(ItemConstants.AVAILABLE_COPIES));
		setPosition(bundle.getString(ItemConstants.POSITION));
		setEdition(bundle.getString(ItemConstants.EDITION));
		setPageCount(bundle.getInt(ItemConstants.PAGE_COUNT));
		setPrice(bundle.getInt(ItemConstants.PRICE, Constants.NOT_SAVED));
		setDetailUrl(bundle.getString(ItemConstants.DETAIL_URL));
		setActive("true".equals(bundle.getString(ItemConstants.IS_ACTIVE)) ? true : false);
		setPublisher(bundle.getString(ItemConstants.PUBLISHER));
	}
	
	/**
	 * Returns the object as a {@link JSONObject}.
	 * @return the object as a {@link JSONObject}
	 */
	public abstract JSONObject toJson();

}
