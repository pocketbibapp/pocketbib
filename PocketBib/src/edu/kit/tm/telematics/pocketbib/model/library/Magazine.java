package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants;

public class Magazine extends Item {

	/**
	 * Creates a new magazine without any metadata.
	 * 
	 * @return the new empty magazine
	 */
	public static Magazine createNew() {
		return new Magazine();
	}

	/**
	 * Creates a magazine object with a unique identifier.
	 * 
	 * @param itemId
	 *            the identifier for this item object in the database
	 * @return the newly created magazine
	 */
	public static Magazine instantiateExisting(int itemId) {
		if (itemId > 0) {
			Magazine m = new Magazine();
			m.itemId = itemId;
			return m;			
		} else {
			throw new IllegalArgumentException("Invalid item ID: " + itemId);
		}
	}
	
	/**
	 * Creates a magazine from a {@link Parcel}.
	 * @param source the parcel
	 * @return the created magazine
	 */
	protected static Magazine create(Parcel source) {
		Magazine m = new Magazine();
		Bundle bundle = source.readBundle();
		
		m.setValuesFromBundle(bundle);
		
		m.setIssn(Issn.initIssn(bundle.getString(ItemConstants.ISSN)));
		m.setPublicationDate((Date) bundle.getSerializable(ItemConstants.PUBLICATION_DATE));
		
		return m;
	}

	/**
	 * The issn.
	 */
	private Issn issn = null;

	/**
	 * The day when this magazine was published.
	 */
	private Date publicationDate = null;

	/**
	 * Returns the issn.
	 * 
	 * @return the issn.
	 */
	public Issn getIssn() {
		return issn;
	}

	/**
	 * Returns the day when this magazine was published.
	 * 
	 * @return the day when this magazine was published.
	 */
	public Date getPublicationDate() {
		return publicationDate;
	}

	@Override
	public String getSecodaryInformation() {
		return publisher;
	}

	/**
	 * Sets the issn.
	 * 
	 * @param issn
	 *            the new issn
	 * @return the magazine
	 */
	public Magazine setIssn(Issn issn) {
		this.issn = issn;
		return this;
	}

	/**
	 * Sets the day when this magazine was published.
	 * 
	 * @param publicationDate
	 *            the new day when this magazine was published.
	 * @return the magazine
	 */
	public Magazine setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
		return this;
	}

	@Override
	public String toString() {
		// @formatter:off
		return "Magazine [availableCopies=" + Integer.toString(this.availableCopies)
			+ ", averageRating=" + Double.toString(getAverageRating())
			+ ", cover=" + (cover != null)
			+ ", edition=" + edition
			+ ", itemId=" + itemId
			+ ", pageCount=" + pageCount
			+ ", position=" + position
			+ ", publisher=" + publisher
			+ ", ratingCount=" + Integer.toString(ratingCount)
			+ ", thumbnail=" + (thumbnail != null)
			+ ", title=" + title
			+ ", issn=" + issn
			+ ", publicationDate=" + ((publicationDate == null) ? null : publicationDate.toString())
			+ ", description=" + description + "]";
		// @formatter:on
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		try {
			if (!Constants.ID_NOT_SAVED.equals(itemId))
				json.putOpt(ItemConstants.ITEM_ID, getItemId());
			
			json.putOpt(ItemConstants.RATING_COUNT, getRatingCount());
			json.putOpt(ItemConstants.TYPE, ItemConstants.TYPE_MAGAZINE);
			json.putOpt(ItemConstants.AVAILABLE_COPIES, getAvailableCopies());
			json.putOpt(ItemConstants.SUM_RATING, getAverageRating());
			json.putOpt(ItemConstants.DESCRIPTION, getDescription());
			json.putOpt(ItemConstants.EDITION, getEdition());
			json.putOpt(ItemConstants.ISSN, (issn == null) ? null : getIssn().getIssn());
			json.putOpt(ItemConstants.PAGE_COUNT, getPageCount());
			json.putOpt(ItemConstants.POSITION, getPosition());
			// json.putOpt(ItemConstants.PUBLICATION_DATE, getPublicationDate().toGMTString());
			json.putOpt(ItemConstants.PUBLISHER, getPublisher());
			json.putOpt(ItemConstants.AUTHOR, getRatingCount());
			json.putOpt(ItemConstants.TITLE, getTitle());
			json.putOpt(ItemConstants.IS_ACTIVE, getActive());
		} catch (JSONException e) {
			e.printStackTrace();
			json = null;
		}
		return json;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle b = super.writeToBundle();
		
		if (issn != null)
			b.putString(ItemConstants.ISSN, issn.getIssn());
		
		b.putSerializable(ItemConstants.PUBLICATION_DATE, publicationDate);
		
		b.writeToParcel(dest, flags);
	}

	/**
	 * A public CREATOR field that generates instances of the {@link Magazine} class from a Parcel. 
	 */
	public static final Parcelable.Creator<Magazine> CREATOR = new Parcelable.Creator<Magazine>() {
		@Override
		public Magazine createFromParcel(Parcel source) {
			return create(source);
		}

		@Override
		public Magazine[] newArray(int size) {
			return new Magazine[size];
		}
	};
}
