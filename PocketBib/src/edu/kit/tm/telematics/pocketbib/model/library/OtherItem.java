package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants;

/**
 * A uncategorized item, which must not be a Book or Magazine.
 */
public class OtherItem extends Item {

	/** the type of the Item (e.g. CD, DVD) */
	private String type = null;

	/** the ISBN of the Item (optional) */
	private Isbn isbn = null;

	/** the ISSN of the Item (optional) */
	private Issn issn = null;

	/** the publication date of the Item (optional) */
	private Date publicationDate = null;

	/** the author of the Item (optional) */
	private String author = null;

	/** the year it was published */
	private int publicationYear = Constants.NOT_SAVED;

	/**
	 * Prepares a new OtherItem library object.
	 * 
	 * @return the created OtherItem
	 */
	public static OtherItem createNew() {
		return new OtherItem();
	}

	/**
	 * Prepares an OtherItem object to be filled with information from the
	 * database.
	 * 
	 * @param itemId
	 *            the ID
	 * @return the OtherItem
	 */
	public static OtherItem instantiateExisting(int itemId) {
		if (itemId > 0) {
			OtherItem o = new OtherItem();
			o.itemId = itemId;
			return o;
		} else {
			throw new IllegalArgumentException("Invalid item ID: " + itemId);
		}
	}

	/**
	 * Creates an <code>OtherItem</code> from a {@link Parcel}.
	 * @param source the parcel
	 * @return the created <code>OtherItem</code>
	 */
	protected static OtherItem create(Parcel source) {
		OtherItem item = new OtherItem();
		Bundle bundle = source.readBundle();
		
		item.setValuesFromBundle(bundle);
		
		item.setIsbn(Isbn.initIsbn(bundle.getString(ItemConstants.ISBN)));
		item.setIssn(Issn.initIssn(bundle.getString(ItemConstants.ISSN)));
		item.setAuthor(bundle.getString(ItemConstants.AUTHOR));
		item.setPublicationYear(bundle.getInt(ItemConstants.PUBLICATION_YEAR, Constants.NOT_SAVED));
		item.setPublicationDate((Date) bundle.getSerializable(ItemConstants.PUBLICATION_DATE));
		item.setType(bundle.getString(ItemConstants.TYPE));
		return item;

	}

	@Override
	public String getSecodaryInformation() {
		return (type == null || type.length() == 0) ? getAuthor() : type;
	}

	/**
	 * Returns the item type.
	 * 
	 * @return the item type
	 */
	public String getType() {
		if (this.type == null) {
			this.type = "OtherItem";
		}
		return type;
	}

	/**
	 * Sets the item type (e.g. CD, DVD, ..)
	 * 
	 * @param type
	 *            the item type
	 * @return the current OtherItem ({@code this})
	 */
	public OtherItem setType(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Returns the ISBN.
	 * 
	 * @return the ISBN
	 */
	public Isbn getIsbn() {
		return isbn;
	}

	/**
	 * Sets the ISBN
	 * 
	 * @param isbn
	 *            the ISBN
	 * @return the current OtherItem ({@code this})
	 */
	public OtherItem setIsbn(Isbn isbn) {
		this.isbn = isbn;
		return this;
	}

	/**
	 * Returns the ISSN.
	 * 
	 * @return the ISSN
	 */
	public Issn getIssn() {
		return issn;
	}

	/**
	 * Sets the ISSN
	 * 
	 * @param issn
	 *            the ISSN
	 * @return the current OtherItem ({@code this})
	 */
	public OtherItem setIssn(Issn issn) {
		this.issn = issn;
		return this;
	}

	/**
	 * Returns the publication date
	 * 
	 * @return the publication date
	 */
	public Date getPublicationDate() {
		return publicationDate;
	}

	/**
	 * Sets the publication date.
	 * 
	 * @param publicationDate
	 *            the publication date
	 * @return the current OtherItem ({@code this})
	 */
	public OtherItem setPublicationDate(Date publicationDate) {
		// Calling setPublicationDate sets publicationYear to the year of
		// publicationDate.

		if (publicationDate == null) {
			this.publicationDate = null;
			this.publicationYear = Constants.NOT_SAVED;
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(publicationDate);

			this.publicationDate = publicationDate;
			this.publicationYear = cal.get(Calendar.YEAR);
		}

		return this;
	}

	/**
	 * Sets the publication year. (And removes the publication date)
	 * 
	 * @param publicationYear
	 *            the publication year
	 * @return the current OtherItem ({@code this})
	 */
	public OtherItem setPublicationYear(Integer publicationYear) {
		// Calling setPublicationYear() removes the publicationDate!
		if (publicationYear != null && publicationYear > 0) {
			this.publicationDate = null;
			this.publicationYear = publicationYear;
		}
		return this;
	}

	/**
	 * Returns the publication year.
	 * @return the publication year
	 */
	public int getPublicationYear() {
		return publicationYear;
	}

	/**
	 * Returns the author
	 * 
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Sets the author
	 * 
	 * @param author
	 *            the author
	 * @return the current OtherItem ({@code this})
	 */
	public OtherItem setAuthor(String author) {
		this.author = author;
		return this;
	}

	@Override
	public OtherItem setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	@Override
	public OtherItem setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	@Override
	public OtherItem setRatingCount(int ratingCount) {
		super.setRatingCount(ratingCount);
		return this;
	}

	@Override
	public OtherItem setAvailableCopies(int availableCopies) {
		super.setAvailableCopies(availableCopies);
		return this;
	}

	@Override
	public OtherItem setPosition(String position) {
		super.setPosition(position);
		return this;
	}

	@Override
	public OtherItem setEdition(String edition) {
		super.setEdition(edition);
		return this;
	}

	@Override
	public OtherItem setPageCount(Integer pageCount) {
		super.setPageCount(pageCount);
		return this;
	}

	@Override
	public OtherItem setPublisher(String publisher) {
		super.setPublisher(publisher);
		return this;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return "OtherItem [availableCopies=" + Integer.toString(this.availableCopies)
				+ ", type=" + type
				+ ", averageRating=" + Double.toString(getAverageRating())
				+ ", cover=" + (cover != null)
				+ ", edition=" + edition
				+ ", itemId=" + itemId
				+ ", pageCount=" + pageCount
				+ ", position=" + position
				+ ", publicationYear=" + ((publicationYear == Constants.NOT_SAVED) ? Constants.NOT_SAVED : Integer.toString(publicationYear))
				+ ", publisher=" + publisher
				+ ", ratingCount=" + Integer.toString(ratingCount)
				+ ", thumbnail=" + (thumbnail != null)
				+ ", title=" + title
				+ ", isbn=" + isbn
				+ ", author=" + author
				+ ", issn=" + issn
				+ ", publicationDate=" + ((publicationDate == null) ? null : publicationDate.toString())
				+ ", description=" + description + "]";
		// @formatter:on
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			json.putOpt(ItemConstants.TYPE, getType());
			json.putOpt(ItemConstants.AUTHOR, getAuthor());
			json.putOpt(ItemConstants.AVAILABLE_COPIES, getAvailableCopies());
			
			if (getSumRating() != null)
				json.putOpt(ItemConstants.SUM_RATING, getSumRating());
			
			json.putOpt(ItemConstants.RATING_COUNT, getRatingCount());
			
			json.putOpt(ItemConstants.DESCRIPTION, getDescription());
			json.putOpt(ItemConstants.EDITION, getEdition());
			
			if(getIsbn() != null)
				json.putOpt(ItemConstants.ISBN, getIsbn().getIsbn13());
			
			if(getIssn() != null)
				json.putOpt(ItemConstants.ISSN, getIssn().getIssn());
			
			if (!Constants.ID_NOT_SAVED.equals(itemId))
				json.putOpt(ItemConstants.ITEM_ID, getItemId());
			
			json.putOpt(ItemConstants.PAGE_COUNT, getPageCount());
			json.putOpt(ItemConstants.POSITION, getPosition());
			json.putOpt(ItemConstants.PUBLICATION_DATE, getPublicationDate());
			json.putOpt(ItemConstants.PUBLICATION_YEAR, getPublicationYear());
			json.putOpt(ItemConstants.PUBLISHER, getPublisher());
			json.putOpt(ItemConstants.AUTHOR, getAuthor());
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
		
		b.putString(ItemConstants.AUTHOR, author);
		b.putSerializable(ItemConstants.PUBLICATION_DATE, publicationDate);
		b.putString(ItemConstants.TYPE, type);
		
		if (publicationYear != Constants.NOT_SAVED)
			b.putInt(ItemConstants.PUBLICATION_YEAR, publicationYear);
		
		if (isbn != null)
			b.putString(ItemConstants.ISBN, isbn.getIsbn13());
		
		if (issn != null)
			b.putString(ItemConstants.ISSN, issn.getIssn());
		
		b.writeToParcel(dest, flags);
	}

	/**
	 * A public CREATOR field that generates instances of the {@link Book} class from a Parcel. 
	 */
	public static final Parcelable.Creator<OtherItem> CREATOR = new Parcelable.Creator<OtherItem>() {
		@Override
		public OtherItem createFromParcel(Parcel source) {
			return create(source);
		}

		@Override
		public OtherItem[] newArray(int size) {
			return new OtherItem[size];
		}
	};

}
