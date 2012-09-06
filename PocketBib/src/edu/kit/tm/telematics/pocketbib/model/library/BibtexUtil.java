package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.Collection;
import java.util.Date;

import edu.kit.tm.telematics.pocketbib.model.Constants;

/**
 * A utility class for creating BibTeX sources of library items.
 */
public class BibtexUtil {

	/** general opening tag for fields */
	private final static String OPEN = "\t= {";

	/** String that ends the current field and inserts a new line (NL) */
	private final static String NL = "},\n";

	/** indentation for fields */
	private final static String INDENT = "\t";

	/** opening tag for a Book-BibTeX */
	private final static String BEGIN_BOOK = "@book{";

	/** opening tag for a Magazine-BibTeX */
	private final static String BEGIN_MAGAZINE = "@article{";

	/** opening tag for a OtherItem-BibTeX */
	private final static String BEGIN_OTHER_ITEM = "@misc{";

	/** end tag for BibTeX elements */
	private final static String END = "}\n\n";

	/** BibTeX author field with indentation and opening tag */
	private final static String FIELD_AUTHOR = INDENT + "author" + OPEN;

	/** BibTeX title field with indentation and opening tag */
	private final static String FIELD_TITLE = INDENT + "title" + OPEN;

	/** BibTeX publisher field with indentation and opening tag */
	private final static String FIELD_PUBLISHER = INDENT + "publisher" + OPEN;

	/** BibTeX year field with indentation and opening tag */
	private final static String FIELD_YEAR = INDENT + "year" + OPEN;

	/** BibTeX journal (magazine) field with indentation and opening tag */
	private final static String FIELD_JOURNAL = INDENT + "journal" + OPEN;

	/** BibTeX month field with indentation and opening tag */
	private final static String FIELD_MONTH = INDENT + "month" + OPEN;

	/** BibTeX day field with indentation and opening tag */
	private final static String FIELD_DAY = INDENT + "day" + OPEN;
	
	/** place holder for missing information. if there should be no place holder use an empty string */
	private final static String MISSING = "MISSING";
	
	/** place holder for the missing citation key. if there should be no place holder use an empty string */
	private final static String MISSING_KEY = INDENT + MISSING + " KEY,\n";
	

	private final static Item[] ITEMS_ARRAY = new Item[0];
	
	/**
	 * Private constructor -- this is a utility class.
	 */
	private BibtexUtil() {
	}

	/**
	 * Returns the BibTeX presentation of the given item / items.
	 * 
	 * @param items
	 *            the items
	 * @return BibTeX-String
	 */
	public static String getBibtex(Item... items) {		
		if (items == null || items.length == 0)
			return "";

		// approximation: 200 characters per BibTeX entry
		StringBuilder builder = new StringBuilder(200 * items.length);

		for (Item item : items) {
			if (item instanceof Book) {
				getBibtex((Book) item, builder);
			} else if (item instanceof Magazine) {
				getBibtex((Magazine) item, builder);
			} else if (item instanceof OtherItem) {
				getBibtex((OtherItem) item, builder);
			} else {
				throw new AssertionError("Item has an unknown type (" + item + ")");
			}
		}

		return builder.toString();
	}
	
	/**
	 * Returns the BibTeX presentation of the given collection of Items
	 * @param items the items
	 * @return BibTeX-String
	 */
	public static String getBibtex(Collection<Item> items) {
		if(items == null || items.size() == 0)
			return "";
		
		return getBibtex(items.toArray(ITEMS_ARRAY));
	}

	/**
	 * Adds the BibTex of a book to a StringBuilder
	 * 
	 * @param book
	 *            the Book
	 * @param builder
	 *            the StringBuilder
	 */
	private static void getBibtex(Book book, StringBuilder builder) {
		builder.append(BEGIN_BOOK).append(MISSING_KEY);
		builder.append(FIELD_AUTHOR).append(book.getAuthor() != null && !book.getAuthor().equals("") ? book.getAuthor() : MISSING).append(NL);
		builder.append(FIELD_TITLE).append(book.getTitle() != null && !book.getTitle().equals("") ? book.getTitle() : MISSING).append(NL);

		if (book.getPublisher() != null && !book.getPublisher().equals(""))
			builder.append(FIELD_PUBLISHER).append(book.getPublisher()).append(NL);

		if (book.getPublicationYear() != Constants.NOT_SAVED)
			builder.append(FIELD_YEAR).append(book.getPublicationYear()).append(NL);

		builder.append(END);
	}

	/**
	 * Adds the BibTex of a magazine (as {@code @article}) to a StringBuilder
	 * 
	 * @param book
	 *            the Magazine
	 * @param builder
	 *            the StringBuilder
	 */
	private static void getBibtex(Magazine mag, StringBuilder builder) {
		builder.append(BEGIN_MAGAZINE).append(MISSING_KEY);
		builder.append(FIELD_JOURNAL).append(mag.getTitle() != null && !mag.getTitle().equals("") ? mag.getTitle() : MISSING).append(NL);
		builder.append(FIELD_AUTHOR).append(MISSING).append(NL); // placeholder
		builder.append(FIELD_TITLE).append(MISSING).append(NL); // placeholder

		if (mag.getPublicationDate() != null) {
			Date date = mag.getPublicationDate();

			builder.append(FIELD_YEAR).append(date.getYear()).append(NL);
			builder.append(FIELD_MONTH).append(date.getMonth()).append(NL);
			builder.append(FIELD_DAY).append(date.getDay()).append(NL);
		}
		builder.append(END);
	}

	/**
	 * Adds the BibTex of a miscellaneous Item to a StringBuilder
	 * 
	 * @param book
	 *            the Item
	 * @param builder
	 *            the StringBuilder
	 */
	private static void getBibtex(OtherItem item, StringBuilder builder) {
		builder.append(BEGIN_OTHER_ITEM).append(MISSING_KEY);
		builder.append(FIELD_AUTHOR).append(item.getAuthor() != null && !item.getAuthor().equals("") ? item.getAuthor() : MISSING).append(NL);
		builder.append(FIELD_TITLE).append(item.getTitle() != null && !item.getTitle().equals("") ? item.getTitle() : MISSING).append(NL);

		if (item.getPublisher() != null && !item.getPublisher().equals("")) {
			builder.append(FIELD_PUBLISHER).append(item.getPublisher()).append(NL);
		}

		if (item.getPublicationDate() != null) {
			Date date = item.getPublicationDate();
			builder.append(FIELD_YEAR).append(date.getYear()).append(NL);
			builder.append(FIELD_MONTH).append(date.getMonth()).append(NL);
			builder.append(FIELD_DAY).append(date.getDay()).append(NL);
		} else if (item.getPublicationYear() != Constants.NOT_SAVED) {
			builder.append(FIELD_YEAR).append(item.getPublicationYear()).append(NL);
		}

		builder.append(END);
	}

}
