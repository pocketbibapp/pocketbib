package edu.kit.tm.telematics.pocketbib.model.library;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.Constants.ItemConstants;

/**
 * A book.
 * 
 * @see Item
 */
public class Book extends Item {
	
	/**
	 * The ISBN for the book.
	 */
	private Isbn isbn = null;
	
	/**
	 * The author of the book.
	 */
	private String author = null;
	
	/**
	 * The publication year of the book.
	 */
	private int publicationYear = Constants.NOT_SAVED;

	/**
	 * Creates a new book without any metadata.
	 * 
	 * @return the new empty book
	 */
	public Book() {
	}
	
	public static Book createNew() {
		return new Book();
	}

	/**
	 * Creates a book object with just an unique identifier.
	 * 
	 * @param itemId
	 *            the identifier for this item object in the database
	 * @return the newly created book
	 */
	public static Book instantiateExisting(int itemId) {
		if (itemId > 0) {
			Book b = new Book();
			b.itemId = itemId;
			return b;
		} else {
			throw new IllegalArgumentException("Invalid item ID: " + itemId);
		}
	}
	
	/**
	 * Creates a Book from a {@link Parcel} created by {@link #writeToParcel(Parcel, int)}.
	 * @param source the book as a parcel object
	 * @return the resulting book
	 */
	protected static Book create(Parcel source) {
		Book b = new Book();
		Bundle bundle = source.readBundle();
		
		b.setValuesFromBundle(bundle);
		
		b.setIsbn(Isbn.initIsbn(bundle.getString(ItemConstants.ISBN)));
		b.setAuthor(bundle.getString(ItemConstants.AUTHOR));
		b.setPublicationYear(bundle.getInt(ItemConstants.PUBLICATION_YEAR));
		return b;
	}

	/**
	 * Returns the author.
	 * 
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Returns the ISBN.
	 * 
	 * @return the ISBN
	 */
	public Isbn getIsbn() {
		return isbn;
	}

	@Override
	public String getSecodaryInformation() {
		return author;
	}

	/**
	 * Sets the author.
	 * 
	 * @param author
	 *            the new author
	 * @return the book
	 */
	public Book setAuthor(String author) {
		this.author = author;
		return this;
	}
	
	/**
	 * Returns the publication year of the book.
	 * @return the publication year of the book
	 */
	public int getPublicationYear() {
		return publicationYear;
	}
	
	
	/**
	 * Sets the year this book was published.
	 * 
	 * @param publicationYear
	 *            the year this book was published
	 * @return the book
	 */	
	@SuppressLint("ParserError")
	public Book setPublicationYear(Integer publicationYear) {
		if (publicationYear != null && publicationYear > 0) {
			this.publicationYear = publicationYear;
		}
		return this;
	}

	/**
	 * Sets the ISBN.
	 * 
	 * @param isbn
	 *            the new ISBN
	 * @return the book
	 */
	public Book setIsbn(Isbn isbn) {
		this.isbn = isbn;
		return this;
	}

	@Override
	public String toString() {
		// @formatter:off
		return "Book [availableCopies=" + Integer.toString(this.availableCopies)
				+ ", averageRating=" + Double.toString(getAverageRating())
				+ ", cover=" + (cover != null)
				+ ", edition=" + edition
				+ ", itemId=" + itemId
				+ ", pageCount=" + pageCount
				+ ", position=" + position
				+ ", publicationYear=" + ((publicationYear == Constants.NOT_SAVED) ? "null" : Integer.toString(publicationYear))
				+ ", publisher=" + publisher
				+ ", ratingCount=" + Integer.toString(ratingCount)
				+ ", thumbnail=" + (thumbnail != null)
				+ ", title=" + title
				+ ", isbn=" + isbn
				+ ", author=" + author
				+ ", price=" + price
				+ ", description=" + description + "]";
		// @formatter:on
	}
	
	/**
	 * Creates a {@link JSONObject} from the current book.
	 * @return the book as a JSON object
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		String isbn = getIsbn() == null ? null : getIsbn().getIsbn13(); 
		
		try {
			if (!Constants.ID_NOT_SAVED.equals(itemId))
				json.putOpt(ItemConstants.ITEM_ID, this.getItemId());
			
			json.putOpt(ItemConstants.TYPE, ItemConstants.TYPE_BOOK);
			json.putOpt(ItemConstants.AUTHOR, this.getAuthor());
			json.putOpt(ItemConstants.AVAILABLE_COPIES, this.getAvailableCopies());
			json.putOpt(ItemConstants.SUM_RATING, this.getSumRating());
			json.putOpt(ItemConstants.DESCRIPTION, this.getDescription());
			json.putOpt(ItemConstants.EDITION, this.getEdition());
			json.putOpt(ItemConstants.ISBN, isbn);
			json.putOpt(ItemConstants.PAGE_COUNT, this.getPageCount());
			json.putOpt(ItemConstants.POSITION, this.getPosition());
			json.putOpt(ItemConstants.PUBLICATION_YEAR, this.getPublicationYear());
			json.putOpt(ItemConstants.PUBLISHER, this.getPublisher());
			json.putOpt(ItemConstants.RATING_COUNT, this.getRatingCount());
			json.putOpt(ItemConstants.TITLE, this.getTitle());
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
		
		if (publicationYear != Constants.NOT_SAVED)
			b.putInt(ItemConstants.PUBLICATION_YEAR, publicationYear);
		
		if (isbn != null)
			b.putString(ItemConstants.ISBN, isbn.getIsbn13());

		b.writeToParcel(dest, flags);
	}

	/**
	 * A public CREATOR field that generates instances of the {@link Book} class from a Parcel. 
	 */
	public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
		@Override
		public Book createFromParcel(Parcel source) {
			return create(source);
		}

		@Override
		public Book[] newArray(int size) {
			return new Book[size];
		}
	};
}
